package com.example.groupproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializationManager {

    private static final String DATA_DIR = "local_cache/";

    static {
        // Ensure directory exists
        new File(DATA_DIR).mkdirs();
    }

    // Save list to file
    public static void saveList(List<?> list, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_DIR + filename))) {
            oos.writeObject(new ArrayList<>(list)); // Convert ObservableList to ArrayList for serialization
            System.out.println("Serialized backup saved: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to save local backup: " + e.getMessage());
        }
    }

    // Load list from file
    @SuppressWarnings("unchecked")
    public static <T> List<T> loadList(String filename) {
        File f = new File(DATA_DIR + filename);
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load local backup: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
