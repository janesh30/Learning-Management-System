package com.example.groupproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LMSApp extends Application {

    private Stage mainStage;
    private ScheduledExecutorService scheduler;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        showLoginScreen();
    }

    // --- LOGIN SCREEN ---
    private void showLoginScreen() {
        StackPane base = new StackPane();
        base.setStyle("-fx-background-color: " + Theme.LIGHT_BG + ";");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(350, 420);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: " + Theme.WHITE_CARD + "; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label logo = new Label("LMS PORTAL");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        logo.setTextFill(Color.web(Theme.PRIMARY_PURPLE));

        TextField uField = Theme.styledTextField("Username");
        PasswordField pField = Theme.styledPasswordField("Password");
        Label msg = new Label();
        msg.setTextFill(Color.RED);

        Button loginBtn = Theme.styledButton("LOGIN", 300);
        Button regBtn = new Button("Create New Account");
        regBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.PRIMARY_PURPLE + "; -fx-cursor: hand;");

        loginBtn.setOnAction(e -> {
            if (DataService.authenticate(uField.getText(), pField.getText())) {
                DataService.refreshData();
                showDashboard();
            } else msg.setText("Invalid Credentials");
        });
        regBtn.setOnAction(e -> showRegister());

        card.getChildren().addAll(logo, new Separator(), uField, pField, loginBtn, regBtn, msg);
        base.getChildren().add(card);

        mainStage.setScene(new Scene(base, 900, 600));
        mainStage.setTitle("Learning Management System Login");
        mainStage.show();
    }

    private void showRegister() {
        Stage s = new Stage();
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + Theme.WHITE_CARD + ";");

        Label title = new Label("Sign Up");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(Theme.PRIMARY_PURPLE));

        TextField u = Theme.styledTextField("New Username");
        PasswordField p = Theme.styledPasswordField("New Password");
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("Student", "Lecturer");
        cb.setPromptText("Select Role");
        cb.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10; -fx-font-size: 14px;");
        cb.setPrefWidth(250);

        Button b = Theme.styledButton("REGISTER", 250);

        b.setOnAction(e -> {
            if(u.getText().isEmpty() || p.getText().isEmpty() || cb.getValue() == null) {
                DataService.showAlert("Please fill all fields"); return;
            }
            String role = cb.getValue().equals("Lecturer") ? "admin" : "student";
            if(DataService.register(u.getText(), p.getText(), role)) {
                DataService.showAlert("Account Created! You can now login."); s.close();
            } else DataService.showAlert("Username already exists.");
        });

        root.getChildren().addAll(title, u, p, cb, b);
        s.setScene(new Scene(root, 350, 400));
        s.show();
    }

    // --- DASHBOARD ---
    private void showDashboard() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: " + Theme.LIGHT_BG + ";");

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: " + Theme.WHITE_CARD + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label welcome = new Label("Welcome, " + DataService.currentUsername);
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        welcome.setTextFill(Color.web(Theme.TEXT_DARK));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: #FFECEC; -fx-text-fill: #D32F2F; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");
        logout.setOnAction(e -> { stopScheduler(); showLoginScreen(); });

        topBar.getChildren().addAll(welcome, spacer, logout);
        layout.setTop(topBar);

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: transparent; -fx-tab-min-height: 40;");

        Tab mainTab = new Tab(DataService.currentUserRole.equalsIgnoreCase("admin") ? "Lecturer Dashboard" : "Student Dashboard");
        mainTab.setClosable(false);
        mainTab.setContent(DataService.currentUserRole.equalsIgnoreCase("admin") ? LecturerPanel.build(mainStage) : StudentPanel.build(mainStage));

        tabs.getTabs().add(mainTab);
        layout.setCenter(tabs);

        mainStage.setScene(new Scene(layout, 1000, 700));
        startScheduler();
    }

    private void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(DataService::refreshData), 10, 10, TimeUnit.SECONDS);
    }

    private void stopScheduler() {
        if(scheduler != null) scheduler.shutdownNow();
    }
}