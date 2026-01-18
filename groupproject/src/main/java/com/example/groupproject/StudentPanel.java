package com.example.groupproject;

import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StudentPanel {

    public static Parent build(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(30));

        // --- Card 1: Notes ---
        VBox notesCard = Theme.createCard("Download Notes");
        ListView<Note> nList = Theme.styledListView();
        nList.setItems(DataService.notes);

        // Double-click to open note
        nList.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && nList.getSelectionModel().getSelectedItem() != null)
                DataService.openFile("LMS_NOTES", nList.getSelectionModel().getSelectedItem().id);
        });
        notesCard.getChildren().add(nList);

        // --- Card 2: Assignments ---
        VBox assignCard = Theme.createCard("My Assignments");
        ListView<Assignment> aList = Theme.styledListView();
        aList.setItems(DataService.assignments);

        // Double-click to open assignment file
        aList.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && aList.getSelectionModel().getSelectedItem() != null)
                DataService.openFile("LMS_ASSIGNMENTS", aList.getSelectionModel().getSelectedItem().id);
        });

        // Custom Cell Factory for Assignment Status Colors
        aList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Assignment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    boolean closed = item.deadline.isBefore(LocalDateTime.now());
                    boolean submitted = DataService.submissions.stream().anyMatch(s -> s.assignmentId == item.id && s.studentName.equals(DataService.currentUsername));

                    String st = (closed ? "[CLOSED]" : "[OPEN]") + (submitted ? " [SUBMITTED]" : " [PENDING]");
                    setText(st + "  " + item.title + "\nDue: " + item.deadline.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));

                    String color = submitted ? "#4CAF50" : (closed ? "#F44336" : Theme.TEXT_DARK);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-padding: 10;");
                }
            }
        });

        Button subBtn = Theme.styledButton("Upload Submission", 200);

        // --- UPLOAD LOGIC WITH NOTIFICATION ---
        subBtn.setOnAction(e -> {
            Assignment sel = aList.getSelectionModel().getSelectedItem();

            if(sel != null && !sel.deadline.isBefore(LocalDateTime.now())) {
                File f = new FileChooser().showOpenDialog(stage);
                if(f != null) {
                    // Upload to DB
                    DataService.uploadFile(f, "INSERT INTO LMS_SUBMISSIONS (assignment_id, student_name, filename, file_data) VALUES(?,?,?,?)", DataService.currentUsername, sel.id);

                    // Trigger Notification
                    NotificationSystem.sendNotification("Submission Successful", "You have successfully submitted: " + sel.title);
                }
            } else {
                DataService.showAlert("Select an OPEN assignment first.");
            }
        });

        assignCard.getChildren().addAll(new Label("Double-click assignment to view instructions"), aList, subBtn);

        root.getChildren().addAll(notesCard, assignCard);
        return new ScrollPane(root);
    }
}