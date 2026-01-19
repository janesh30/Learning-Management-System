package com.example.groupproject;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

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