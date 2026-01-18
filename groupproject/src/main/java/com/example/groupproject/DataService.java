package com.example.groupproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;

public class DataService {

    // Shared Data Lists
    public static final ObservableList<Note> notes = FXCollections.observableArrayList();
    public static final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    public static final ObservableList<Submission> submissions = FXCollections.observableArrayList();

    // Session Info
    public static String currentUserRole = "";
    public static String currentUsername = "";

    public static boolean authenticate(String u, String p) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT ROLE FROM LMS_USERS WHERE USERNAME=? AND PASSWORD=?")) {
            s.setString(1, u);
            s.setString(2, p);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                currentUserRole = r.getString("ROLE");
                currentUsername = u;
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public static boolean register(String u, String p, String r) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement("INSERT INTO LMS_USERS VALUES(?,?,?)")) {
            s.setString(1, u);
            s.setString(2, p);
            s.setString(3, r);
            s.executeUpdate();
            return true;
        } catch (Exception e) { return false; }
    }

    public static void refreshData() {
        notes.clear(); assignments.clear(); submissions.clear();
        try (Connection c = DatabaseConnection.getConnection()) {
            ResultSet r1 = c.createStatement().executeQuery("SELECT * FROM LMS_NOTES");
            while (r1.next()) notes.add(new Note(r1.getInt("id"), r1.getString("title"), r1.getString("filename"), r1.getTimestamp("uploaded_at").toLocalDateTime()));

            ResultSet r2 = c.createStatement().executeQuery("SELECT * FROM LMS_ASSIGNMENTS");
            while (r2.next()) assignments.add(new Assignment(r2.getInt("id"), r2.getString("title"), r2.getString("filename"), r2.getTimestamp("deadline").toLocalDateTime()));

            ResultSet r3 = c.createStatement().executeQuery("SELECT * FROM LMS_SUBMISSIONS");
            while (r3.next()) submissions.add(new Submission(r3.getInt("id"), r3.getInt("assignment_id"), r3.getString("student_name"), r3.getString("filename"), r3.getTimestamp("submitted_at").toLocalDateTime()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void uploadFile(File f, String sql, String p1, int p2) {
        try (Connection c = DatabaseConnection.getConnection();
             FileInputStream fis = new FileInputStream(f);
             PreparedStatement p = c.prepareStatement(sql)) {
            if (p2 == -1) { // Note upload
                p.setString(1, p1);
                p.setString(2, f.getName());
                p.setBinaryStream(3, fis, (int) f.length());
            } else { // Submission upload
                p.setInt(1, p2);
                p.setString(2, p1);
                p.setString(3, f.getName());
                p.setBinaryStream(4, fis, (int) f.length());
            }
            p.executeUpdate();
            refreshData();
            showAlert("Success!");
        } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    public static void openFile(String table, int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT filename, file_data FROM " + table + " WHERE id=?")) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                Blob blob = r.getBlob("file_data");
                if (blob == null) { showAlert("No file found."); return; }
                File t = new File(System.getProperty("java.io.tmpdir") + File.separator + r.getString("filename"));
                try (InputStream in = blob.getBinaryStream(); FileOutputStream out = new FileOutputStream(t)) {
                    in.transferTo(out);
                }
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(t);
            }
        } catch (Exception e) { showAlert(e.getMessage()); }
    }

    public static void showAlert(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).show();
    }
}