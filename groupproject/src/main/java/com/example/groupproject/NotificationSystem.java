package com.example.groupproject;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class NotificationSystem {

    // --- State Tracking (to prevent spamming) ---
    private static final Set<Integer> alertedNearDue = new HashSet<>();
    private static final Set<Integer> alertedDeadlineReached = new HashSet<>();

    // Initialize as -1 so we don't alert for existing items on login
    private static int lastNoteCount = -1;
    private static int lastAssignmentCount = -1;
    private static int lastSubmissionCount = -1;

    // --- STUDENT LOGIC ---
    public static void checkStudent(ObservableList<Assignment> assignments, ObservableList<Note> notes) {
        // 1. Check for New Notes
        if (lastNoteCount != -1 && notes.size() > lastNoteCount) {
            sendNotification("New Material", "A lecturer has uploaded a new note.");
        }
        lastNoteCount = notes.size();

        // 2. Check for New Assignments
        if (lastAssignmentCount != -1 && assignments.size() > lastAssignmentCount) {
            sendNotification("New Assignment", "A new assignment has been posted.");
        }
        lastAssignmentCount = assignments.size();

        // 3. Check for Due Dates Nearing (1 Hour)
        LocalDateTime now = LocalDateTime.now();
        for (Assignment a : assignments) {
            long minutesUntil = ChronoUnit.MINUTES.between(now, a.deadline);
            // If due in 0-60 mins and hasn't been closed/alerted yet
            if (minutesUntil > 0 && minutesUntil <= 60 && !alertedNearDue.contains(a.id)) {
                sendNotification("Deadline Warning", "Assignment '" + a.title + "' is due in less than 1 hour!");
                alertedNearDue.add(a.id);
            }
        }
    }

    // --- LECTURER LOGIC ---
    public static void checkLecturer(ObservableList<Submission> submissions, ObservableList<Assignment> assignments) {
        // 1. Check for New Submissions
        if (lastSubmissionCount != -1 && submissions.size() > lastSubmissionCount) {
            sendNotification("New Submission", "A student has submitted an assignment.");
        }
        lastSubmissionCount = submissions.size();

        // 2. Check for Deadlines Reached
        LocalDateTime now = LocalDateTime.now();
        for (Assignment a : assignments) {
            if (a.deadline.isBefore(now) && !alertedDeadlineReached.contains(a.id)) {
                sendNotification("Deadline Reached", " The deadline for '" + a.title + "' has passed.");
                alertedDeadlineReached.add(a.id);
            }
        }
    }

    // --- GENERAL NOTIFICATION ---
    public static void sendNotification(String title, String message) {
        // Run on UI thread to be safe
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(title);
            // Optional: Make it non-modal (doesn't block the whole app)
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            alert.show();
        });
    }
}