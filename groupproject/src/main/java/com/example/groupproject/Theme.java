package com.example.groupproject;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Theme {
    public static final String PRIMARY_PURPLE = "#6A0DAD";
    public static final String LIGHT_BG = "#F3F4F6";
    public static final String WHITE_CARD = "#FFFFFF";
    public static final String TEXT_DARK = "#2D3748";

    public static TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-color: #F7F7F7; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-padding: 0 15 0 15;");
        return tf;
    }

    public static PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(40);
        pf.setStyle("-fx-background-color: #F7F7F7; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-padding: 0 15 0 15;");
        return pf;
    }

    public static Button styledButton(String text, double width) {
        Button btn = new Button(text);
        btn.setPrefWidth(width);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: " + PRIMARY_PURPLE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #550a8a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PRIMARY_PURPLE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
        return btn;
    }

    public static VBox createCard(String titleText) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + WHITE_CARD + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(PRIMARY_PURPLE));

        card.getChildren().add(title);
        return card;
    }

    public static <T> ListView<T> styledListView() {
        ListView<T> lv = new ListView<>();
        lv.setPrefHeight(150);
        lv.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #FAFAFA; -fx-background-radius: 5;");
        return lv;
    }
}