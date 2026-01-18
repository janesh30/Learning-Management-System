package com.example.groupproject;

import java.time.LocalDateTime;

public class Submission {
    public int id;
    public int assignmentId;
    public String studentName;
    public String filename;
    public LocalDateTime submittedAt;

    public Submission(int id, int assignmentId, String studentName, String filename, LocalDateTime submittedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentName = studentName;
        this.filename = filename;
        this.submittedAt = submittedAt;
    }

    @Override
    public String toString() {
        return studentName + " - " + filename;
    }
}