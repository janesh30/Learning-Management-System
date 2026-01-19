package com.example.groupproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.io.*;
import java.sql.*;
import java.util.List;

public class DataService {

    // Shared Data Lists
    public static final ObservableList<Note> notes = FXCollections.observableArrayList();
    public static final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    public static final ObservableList<Submission> submissions = FXCollections.observableArrayList();

    // Session Info
    public static String currentUserRole = "";
    public static String currentUsername = "";

    // --- AUTHENTICATION ---
    public static boolean authenticate(String u, String p) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT ROLE FROM LMS_USERS WHERE USERNAME=? AND PASSWORD=?")) {
            s.setString(1, u); s.setString(2, p);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                currentUserRole = r.getString("ROLE");
                currentUsername = u;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Optional: You could allow offline login here if you serialized User objects too
        }
        return false;
    }

    public static boolean register(String u, String p, String r) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement("INSERT INTO LMS_USERS VALUES(?,?,?)")) {
            s.setString(1, u); s.setString(2, p); s.setString(3, r);
            s.executeUpdate();
            return true;
        } catch (Exception e) { return false; }
    }

    // --- DATA REFRESH & NOTIFICATIONS ---
    public static void refreshData() {
        boolean dbSuccess = false;

        try (Connection c = DatabaseConnection.getConnection()) {
            // Temporary lists to hold DB data
            List<Note> dbNotes = new java.util.ArrayList<>();
            List<Assignment> dbAssignments = new java.util.ArrayList<>();
            List<Submission> dbSubmissions = new java.util.ArrayList<>();

            // 1. Fetch Notes
            ResultSet r1 = c.createStatement().executeQuery("SELECT * FROM LMS_NOTES");
            while (r1.next()) dbNotes.add(new Note(r1.getInt("id"), r1.getString("title"), r1.getString("filename"), r1.getTimestamp("uploaded_at").toLocalDateTime()));

            // 2. Fetch Assignments
            ResultSet r2 = c.createStatement().executeQuery("SELECT * FROM LMS_ASSIGNMENTS");
            while (r2.next()) dbAssignments.add(new Assignment(r2.getInt("id"), r2.getString("title"), r2.getString("filename"), r2.getTimestamp("deadline").toLocalDateTime()));

            // 3. Fetch Submissions
            ResultSet r3 = c.createStatement().executeQuery("SELECT * FROM LMS_SUBMISSIONS");
            while (r3.next()) dbSubmissions.add(new Submission(r3.getInt("id"), r3.getInt("assignment_id"), r3.getString("student_name"), r3.getString("filename"), r3.getTimestamp("submitted_at").toLocalDateTime()));

            // Update Observable Lists
            notes.setAll(dbNotes);
            assignments.setAll(dbAssignments);
            submissions.setAll(dbSubmissions);

            dbSuccess = true;

            // --- SERIALIZATION: SAVE BACKUP ---
            SerializationManager.saveList(notes, "notes.ser");
            SerializationManager.saveList(assignments, "assignments.ser");
            SerializationManager.saveList(submissions, "submissions.ser");

            // --- TRIGGER NOTIFICATIONS ---
            if (currentUserRole.equalsIgnoreCase("student")) {
                NotificationSystem.checkStudent(assignments, notes);
            } else if (currentUserRole.equalsIgnoreCase("admin")) {
                NotificationSystem.checkLecturer(submissions, assignments);
            }

        } catch (Exception e) {
            System.err.println("DB Connection failed, attempting to load from Local Cache...");
        }

        // --- OFFLINE MODE: LOAD FROM BACKUP IF DB FAILED ---
        if (!dbSuccess) {
            notes.setAll(SerializationManager.loadList("notes.ser"));
            assignments.setAll(SerializationManager.loadList("assignments.ser"));
            submissions.setAll(SerializationManager.loadList("submissions.ser"));
            System.out.println("Loaded data from local serialization cache.");
        }
    }

    // --- FILE OPERATIONS (Requires DB) ---
    public static void uploadFile(File f, String sql, String p1, int p2) {
        try (Connection c = DatabaseConnection.getConnection();
             FileInputStream fis = new FileInputStream(f);
             PreparedStatement p = c.prepareStatement(sql)) {

            if (p2 == -1) {
                p.setString(1, p1);
                p.setString(2, f.getName());
                p.setBinaryStream(3, fis, (int) f.length());
            } else {
                p.setInt(1, p2);
                p.setString(2, p1);
                p.setString(3, f.getName());
                p.setBinaryStream(4, fis, (int) f.length());
            }

            p.executeUpdate();
            refreshData();
            showAlert("Success!");

        } catch (Exception e) { showAlert("Upload Error (Check DB Connection): " + e.getMessage()); }
    }

    public static void openFile(String table, int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT filename, file_data FROM " + table + " WHERE id=?")) {
            s.setInt(1, id); ResultSet r = s.executeQuery();
            if (r.next()) {
                Blob blob = r.getBlob("file_data");
                if (blob == null) { showAlert("No file found."); return; }

                File t = new File(System.getProperty("java.io.tmpdir") + File.separator + r.getString("filename"));
                try (InputStream in = blob.getBinaryStream(); FileOutputStream out = new FileOutputStream(t)) {
                    in.transferTo(out);
                }
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(t);
            }
        } catch (Exception e) { showAlert("Download Error (Check DB Connection): " + e.getMessage()); }
    }

    public static void showAlert(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).show();
    }
}