package com.example.groupproject;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LecturerPanel {
    private static File selectedAssignmentFile = null;

    public static Parent build(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(30));

        // --- Card 1: Notes ---
        VBox notesCard = Theme.createCard("Course Material");
        TextField nTitle = Theme.styledTextField("Note Title");
        Button nUp = Theme.styledButton("Upload Note", 150);
        ListView<Note> nList = Theme.styledListView();

        nUp.setOnAction(e -> {
            File f = new FileChooser().showOpenDialog(stage);
            if(f != null) DataService.uploadFile(f, "INSERT INTO LMS_NOTES (title, filename, file_data) VALUES(?,?,?)", nTitle.getText().isEmpty() ? f.getName() : nTitle.getText(), -1);
        });

        nList.setItems(DataService.notes);
        nList.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && nList.getSelectionModel().getSelectedItem() != null)
                DataService.openFile("LMS_NOTES", nList.getSelectionModel().getSelectedItem().id);
        });

        HBox noteControls = new HBox(10, nTitle, nUp);
        notesCard.getChildren().addAll(noteControls, nList);

        // --- Card 2: Assignments ---
        VBox assignCard = Theme.createCard("Assignments & Submissions");
        TextField aTitle = Theme.styledTextField("Assignment Title");
        DatePicker dp = new DatePicker(); dp.setPrefHeight(40);
        TextField tf = Theme.styledTextField("HH:mm"); tf.setMaxWidth(80);

        Button chooseFile = new Button("Attach File");
        chooseFile.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 10;");
        Label fileLbl = new Label("No file");
        Button createA = Theme.styledButton("Create", 120);

        chooseFile.setOnAction(e -> {
            selectedAssignmentFile = new FileChooser().showOpenDialog(stage);
            if(selectedAssignmentFile != null) fileLbl.setText(selectedAssignmentFile.getName());
        });

        createA.setOnAction(e -> {
            if(aTitle.getText().isEmpty() || dp.getValue() == null || selectedAssignmentFile == null) {
                DataService.showAlert("Fill all fields"); return;
            }
            try {
                LocalDateTime dt = dp.getValue().atTime(Integer.parseInt(tf.getText().split(":")[0]), Integer.parseInt(tf.getText().split(":")[1]));
                try(Connection c = DatabaseConnection.getConnection();
                    FileInputStream fis = new FileInputStream(selectedAssignmentFile);
                    PreparedStatement p = c.prepareStatement("INSERT INTO LMS_ASSIGNMENTS (title, deadline, filename, file_data) VALUES(?,?,?,?)")) {
                    p.setString(1, aTitle.getText());
                    p.setTimestamp(2, Timestamp.valueOf(dt));
                    p.setString(3, selectedAssignmentFile.getName());
                    p.setBinaryStream(4, fis, (int)selectedAssignmentFile.length());
                    p.executeUpdate();
                    DataService.refreshData();
                    DataService.showAlert("Created!");
                    aTitle.clear(); fileLbl.setText("No file"); selectedAssignmentFile = null;
                }
            } catch(Exception ex) { DataService.showAlert("Error: " + ex.getMessage()); }
        });

        ListView<Assignment> aList = Theme.styledListView();
        aList.setItems(DataService.assignments);
        ListView<Submission> sList = Theme.styledListView();

        aList.getSelectionModel().selectedItemProperty().addListener((o, old, v) -> {
            if(v != null) sList.setItems(DataService.submissions.filtered(s -> s.assignmentId == v.id));
        });

        sList.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && sList.getSelectionModel().getSelectedItem() != null)
                DataService.openFile("LMS_SUBMISSIONS", sList.getSelectionModel().getSelectedItem().id);
        });

        HBox assignControls = new HBox(10, aTitle, dp, tf, chooseFile, fileLbl, createA);
        assignControls.setAlignment(Pos.CENTER_LEFT);
        assignCard.getChildren().addAll(assignControls, new Label("Select Assignment to view submissions:"), aList, new Separator(), new Label("Student Submissions:"), sList);

        root.getChildren().addAll(notesCard, assignCard);
        return new ScrollPane(root);
    }
}