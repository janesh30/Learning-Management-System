package com.example.groupproject;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String title;
    public String filename;
    public LocalDateTime uploadedAt;

    public Note(int id, String title, String filename, LocalDateTime uploadedAt) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return title + " (" + filename + ")";
    }
}