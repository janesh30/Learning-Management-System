package com.example.groupproject;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String title;
    public String filename;
    public LocalDateTime deadline;

    public Assignment(int id, String title, String filename, LocalDateTime deadline) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return title + " (" + filename + ")";
    }
}