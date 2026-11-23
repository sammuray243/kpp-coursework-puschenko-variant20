
package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import ua.edu.nupp.kpp.client.csv.CsvImporter;
import ua.edu.nupp.kpp.client.http.ApiClient;
import ua.edu.nupp.kpp.client.model.Route;
import ua.edu.nupp.kpp.client.model.Vehicle;
import ua.edu.nupp.kpp.client.model.Incident;
import ua.edu.nupp.kpp.client.model.ResponseTask;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainApp extends Application {

    private final ApiClient apiClient = new ApiClient();

    private final TableView<Route> routeTable = new TableView<>();
    private final TableView<Vehicle> vehicleTable = new TableView<>();
    private final TableView<Incident> incidentTable = new TableView<>();
    private final TableView<ResponseTask> taskTable = new TableView<>();

    private final Label statusLabel = new Label("Готово до роботи");

    private final Button refreshButton = new Button("Оновити дані");
    private final Button addButton = new Button("Додати");
    private final Button editButton = new Button("Редагувати");
    private final Button deleteButton = new Button("Видалити");
    private final Button statusButton = new Button("Змінити статус");
    private final Button importButton = new Button("Імпорт CSV");

    private final ProgressIndicator progress = new ProgressIndicator();
    private final ProgressBar importProgress = new ProgressBar(0);
    private final Label importStatusLabel = new Label("Імпорт не запущено");

    private TabPane tabPane;
    private boolean busy = false;
    private boolean importing = false;

    @Override
    public void start(Stage stage) {

        Label title = new Label(
                "Інформаційна система реєстрації " +
                        "інцидентів громадського транспорту"
        );
        title.getStyleClass().add("app-title");

        Label subtitle = new Label(
                "Курсова робота | Варіант №20"
        );
        subtitle.getStyleClass().add("app-subtitle");

        VBox header = new VBox(6, title, subtitle);
        header.getStyleClass().add("header");

        configureRouteTable();
        configureVehicleTable();
        configureIncidentTable();
        configureTaskTable();

        tabPane = new TabPane();

        Tab routesTab = new Tab("Маршрути", routeTable);
        Tab vehiclesTab = new Tab("Транспорт", vehicleTable);
        Tab incidentsTab = new Tab("Інциденти", incidentTable);
        Tab tasksTab = new Tab("Завдання реагування", taskTable);

        for (Tab tab : new Tab[]{
                routesTab, vehiclesTab, incidentsTab, tasksTab
        }) {
            tab.setClosable(false);
        }

        tabPane.getTabs().addAll(
                routesTab, vehiclesTab, incidentsTab, tasksTab
        );

        refreshButton.setOnAction(e -> refreshCurrentTab());
        addButton.setOnAction(e -> addCurrent());
        editButton.setOnAction(e -> editCurrent());
        deleteButton.setOnAction(e -> deleteCurrent());
        statusButton.setOnAction(e -> changeIncidentStatus());
        importButton.setOnAction(e -> importCsv(stage));

        addButton.getStyleClass().add("btn-success");
        editButton.getStyleClass().add("btn-primary");
        deleteButton.getStyleClass().add("btn-danger");
        statusButton.getStyleClass().add("btn-purple");
        importButton.getStyleClass().add("btn-import");

        statusLabel.getStyleClass().add("status-label");

        progress.setPrefSize(22, 22);
        progress.setVisible(false);
        progress.setManaged(false);

        importProgress.setPrefWidth(220);
        importProgress.setVisible(false);
        importProgress.setManaged(false);

        importStatusLabel.setVisible(false);
        importStatusLabel.setManaged(false);
        importStatusLabel.setWrapText(true);

        HBox toolbar = new HBox(
                10,
                refreshButton,
                addButton,
                editButton,
                deleteButton,
                statusButton,
                importButton,
                progress,
                statusLabel
        );

        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        HBox importPanel = new HBox(
                12,
                new Label("CSV-імпорт:"),
                importProgress,
                importStatusLabel
        );

        importPanel.setAlignment(Pos.CENTER_LEFT);
        importPanel.getStyleClass().add("import-panel");

        VBox center = new VBox(
                10,
                toolbar,
                importPanel,
                tabPane
        );

        center.getStyleClass().add("main-content");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(center);

        Scene scene = new Scene(root, 1200, 680);

        URL cssUrl = getClass().getResource("/style.css");

        if (cssUrl != null) {
            scene.getStylesheets().add(
                    cssUrl.toExternalForm()
            );
        } else {
            System.err.println(
                    "Увага: файл /style.css не знайдено."
            );
        }

        stage.setTitle("Transport Incidents V20");
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(500);
        stage.show();

        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    updateButtons();
                    if (!busy && !importing) {
                        refreshCurrentTab();
                    }
                });

        routeTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) ->
                        updateButtons());

        vehicleTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) ->
                        updateButtons());

        incidentTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) ->
                        updateButtons());

        taskTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) ->
                        updateButtons());

        updateButtons();
        refreshCurrentTab();
    }

    // =====================================
    // НАЛАШТУВАННЯ ТАБЛИЦЬ
    // =====================================

    private void configureRouteTable() {

        routeTable.getColumns().addAll(
                column("ID", Route::getId),
                column("Номер маршруту", Route::getRouteNumber),
                column("Назва маршруту", Route::getRouteName),
                column("Важливість", Route::getImportance)
        );

        setupTable(routeTable, "Маршрути відсутні");
    }

    private void configureVehicleTable() {

        vehicleTable.getColumns().addAll(
                column("ID", Vehicle::getId),
                column(
                        "Реєстраційний номер",
                        Vehicle::getRegistrationNumber
                ),
                column("Тип транспорту", Vehicle::getVehicleType),
                column("Модель", Vehicle::getModel),
                column("ID маршруту", Vehicle::getRouteId)
        );

        setupTable(vehicleTable, "Транспорт відсутній");
    }

    private void configureIncidentTable() {

        incidentTable.getColumns().addAll(
                column("ID", Incident::getId),
                column("ID транспорту", Incident::getVehicleId),
                column("ID маршруту", Incident::getRouteId),
                column("Опис", Incident::getDescription),
                column("Дата", Incident::getIncidentDate),
                column("Статус", Incident::getStatus),
                column("F1", Incident::getF1),
                column("F2", Incident::getF2),
                column("F3", Incident::getF3),
                column(
                        "Показник I",
                        incident -> String.format(
                                Locale.US,
                                "%.2f",
                                incident.getIntegralScore()
                        )
                )
        );

        setupTable(incidentTable, "Інциденти відсутні");
    }

    private void configureTaskTable() {

        taskTable.getColumns().addAll(
                column("ID", ResponseTask::getId),
                column("ID інциденту", ResponseTask::getIncidentId),
                column(
                        "Опис завдання",
                        ResponseTask::getTaskDescription
                ),
                column(
                        "Відповідальний",
                        ResponseTask::getResponsiblePerson
                )
        );

        setupTable(taskTable, "Завдання відсутні");
    }

    private <T> void setupTable(
            TableView<T> table,
            String placeholder
    ) {

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setPlaceholder(new Label(placeholder));
    }

    private <T, V> TableColumn<T, V> column(
            String title,
            Function<T, V> extractor
    ) {

        TableColumn<T, V> result = new TableColumn<>(title);

        result.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        extractor.apply(data.getValue())
                )
        );

        return result;
    }

    // =====================================
    // КЕРУВАННЯ КНОПКАМИ
    // =====================================

    private void updateButtons() {

        if (tabPane == null) {
            return;
        }

        int index = tabPane.getSelectionModel()
                .getSelectedIndex();

        boolean selected = switch (index) {
            case 0 -> routeTable.getSelectionModel()
                    .getSelectedItem() != null;
            case 1 -> vehicleTable.getSelectionModel()
                    .getSelectedItem() != null;
            case 2 -> incidentTable.getSelectionModel()
                    .getSelectedItem() != null;
            case 3 -> taskTable.getSelectionModel()
                    .getSelectedItem() != null;
            default -> false;
        };

        addButton.setDisable(busy || importing || index < 0);

        editButton.setDisable(
                busy || importing || !selected || index == 2
        );

        deleteButton.setDisable(busy || importing || !selected);

        statusButton.setVisible(index == 2);
        statusButton.setManaged(index == 2);
        statusButton.setDisable(
                busy || importing || index != 2 || !selected
        );

        boolean csvTab = index == 1 || index == 2;

        importButton.setVisible(csvTab);
        importButton.setManaged(csvTab);
        importButton.setDisable(busy || importing);

        refreshButton.setDisable(busy || importing);
    }

    private void addCurrent() {

        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 0 -> addRoute();
            case 1 -> addVehicle();
            case 2 -> addIncident();
            case 3 -> addResponseTask();
            default -> {
            }
        }
    }

    private void editCurrent() {

        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 0 -> editRoute();
            case 1 -> editVehicle();
            case 3 -> editResponseTask();
            default -> {
            }
        }
    }

    private void deleteCurrent() {

        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 0 -> deleteRoute();
            case 1 -> deleteVehicle();
            case 2 -> deleteIncident();
            case 3 -> deleteResponseTask();
            default -> {
            }
        }
    }

    // =====================================
    // CRUD МАРШРУТІВ
    // =====================================

    private void addRoute() {

        showRouteDialog(null).ifPresent(route ->
                runOperation(
                        "Створення маршруту...",
                        () -> apiClient.createRoute(route),
                        this::loadRoutes
                )
        );
    }

    private void editRoute() {

        Route selected = routeTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери маршрут.");
            return;
        }

        showRouteDialog(selected).ifPresent(route ->
                runOperation(
                        "Оновлення маршруту...",
                        () -> apiClient.updateRoute(route),
                        this::loadRoutes
                )
        );
    }

    private void deleteRoute() {

        Route selected = routeTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери маршрут.");
            return;
        }

        if (confirmDelete(
                "Видалити маршрут №" +
                        selected.getRouteNumber() + "?"
        )) {
            runOperation(
                    "Видалення маршруту...",
                    () -> apiClient.deleteRoute(selected.getId()),
                    this::loadRoutes
            );
        }
    }

    private Optional<Route> showRouteDialog(Route existing) {

        boolean editing = existing != null;

        Dialog<Route> dialog = new Dialog<>();

        dialog.setTitle(
                editing
                        ? "Редагування маршруту"
                        : "Новий маршрут"
        );

        ButtonType saveType = new ButtonType(
                "Зберегти",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType,
                ButtonType.CANCEL
        );

        TextField numberField = new TextField();
        TextField nameField = new TextField();
        TextField importanceField = new TextField();

        if (editing) {
            numberField.setText(existing.getRouteNumber());
            nameField.setText(existing.getRouteName());
            importanceField.setText(
                    String.valueOf(existing.getImportance())
            );
        }

        GridPane grid = createFormGrid();

        grid.addRow(
                0,
                new Label("Номер маршруту:"),
                numberField
        );

        grid.addRow(
                1,
                new Label("Назва маршруту:"),
                nameField
        );

        grid.addRow(
                2,
                new Label("Важливість (0–100):"),
                importanceField
        );

        Label errorLabel = createErrorLabel();
        grid.add(errorLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        saveButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    try {
                        validateRoute(
                                numberField.getText(),
                                nameField.getText(),
                                importanceField.getText()
                        );
                        errorLabel.setText("");
                    } catch (IllegalArgumentException e) {
                        errorLabel.setText(e.getMessage());
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(button -> {

            if (button != saveType) {
                return null;
            }

            Route route = new Route();

            route.setId(editing ? existing.getId() : 0);
            route.setRouteNumber(numberField.getText().trim());
            route.setRouteName(nameField.getText().trim());
            route.setImportance(
                    Integer.parseInt(
                            importanceField.getText().trim()
                    )
            );

            return route;
        });

        return dialog.showAndWait();
    }

    private void validateRoute(
            String number,
            String name,
            String importanceText
    ) {

        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException(
                    "Вкажи номер маршруту."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Вкажи назву маршруту."
            );
        }

        int importance;

        try {
            importance = Integer.parseInt(
                    importanceText.trim()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Важливість повинна бути цілим числом."
            );
        }

        if (importance < 0 || importance > 100) {
            throw new IllegalArgumentException(
                    "Важливість повинна бути від 0 до 100."
            );
        }
    }

    // =====================================
    // CRUD ТРАНСПОРТУ
    // =====================================

    private void addVehicle() {

        showVehicleDialog(null).ifPresent(vehicle ->
                runOperation(
                        "Створення транспорту...",
                        () -> apiClient.createVehicle(vehicle),
                        this::loadVehicles
                )
        );
    }

    private void editVehicle() {

        Vehicle selected = vehicleTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери транспорт.");
            return;
        }

        showVehicleDialog(selected).ifPresent(vehicle ->
                runOperation(
                        "Оновлення транспорту...",
                        () -> apiClient.updateVehicle(vehicle),
                        this::loadVehicles
                )
        );
    }

    private void deleteVehicle() {

        Vehicle selected = vehicleTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери транспорт.");
            return;
        }

        if (confirmDelete(
                "Видалити транспорт " +
                        selected.getRegistrationNumber() + "?"
        )) {
            runOperation(
                    "Видалення транспорту...",
                    () -> apiClient.deleteVehicle(selected.getId()),
                    this::loadVehicles
            );
        }
    }

    private Optional<Vehicle> showVehicleDialog(
            Vehicle existing
    ) {

        List<Route> routes;

        try {
            routes = apiClient.getRoutes();
        } catch (Exception e) {
            showError(
                    "Помилка завантаження маршрутів",
                    getErrorMessage(e)
            );
            return Optional.empty();
        }

        if (routes.isEmpty()) {
            showWarning("Спочатку створи маршрут.");
            return Optional.empty();
        }

        boolean editing = existing != null;

        Dialog<Vehicle> dialog = new Dialog<>();

        dialog.setTitle(
                editing
                        ? "Редагування транспорту"
                        : "Новий транспорт"
        );

        ButtonType saveType = new ButtonType(
                "Зберегти",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType,
                ButtonType.CANCEL
        );

        TextField numberField = new TextField();
        TextField typeField = new TextField();
        TextField modelField = new TextField();

        ComboBox<Route> routeCombo = new ComboBox<>(
                FXCollections.observableArrayList(routes)
        );

        routeCombo.setMaxWidth(Double.MAX_VALUE);

        if (editing) {

            numberField.setText(
                    existing.getRegistrationNumber()
            );

            typeField.setText(existing.getVehicleType());
            modelField.setText(existing.getModel());

            for (Route route : routes) {
                if (route.getId() == existing.getRouteId()) {
                    routeCombo.setValue(route);
                    break;
                }
            }
        }

        GridPane grid = createFormGrid();

        grid.addRow(
                0,
                new Label("Реєстраційний номер:"),
                numberField
        );

        grid.addRow(
                1,
                new Label("Тип транспорту:"),
                typeField
        );

        grid.addRow(
                2,
                new Label("Модель:"),
                modelField
        );

        grid.addRow(
                3,
                new Label("Маршрут:"),
                routeCombo
        );

        Label errorLabel = createErrorLabel();
        grid.add(errorLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        saveButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    try {
                        validateVehicle(
                                numberField.getText(),
                                typeField.getText(),
                                modelField.getText(),
                                routeCombo.getValue()
                        );
                        errorLabel.setText("");
                    } catch (IllegalArgumentException e) {
                        errorLabel.setText(e.getMessage());
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(button -> {

            if (button != saveType) {
                return null;
            }

            Vehicle vehicle = new Vehicle();

            vehicle.setId(editing ? existing.getId() : 0);
            vehicle.setRegistrationNumber(
                    numberField.getText().trim()
            );
            vehicle.setVehicleType(typeField.getText().trim());
            vehicle.setModel(modelField.getText().trim());
            vehicle.setRouteId(routeCombo.getValue().getId());

            return vehicle;
        });

        return dialog.showAndWait();
    }

    private void validateVehicle(
            String number,
            String type,
            String model,
            Route route
    ) {

        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException(
                    "Вкажи реєстраційний номер."
            );
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Вкажи тип транспорту."
            );
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException(
                    "Вкажи модель."
            );
        }

        if (route == null) {
            throw new IllegalArgumentException(
                    "Вибери маршрут."
            );
        }
    }

    // =====================================
    // ІНЦИДЕНТИ
    // =====================================

    private void addIncident() {

        showIncidentDialog().ifPresent(incident ->
                runOperation(
                        "Реєстрація інциденту...",
                        () -> apiClient.createIncident(incident),
                        this::loadIncidents
                )
        );
    }

    private Optional<Incident> showIncidentDialog() {

        List<Route> routes;
        List<Vehicle> vehicles;

        try {
            routes = apiClient.getRoutes();
            vehicles = apiClient.getVehicles();
        } catch (Exception e) {
            showError(
                    "Помилка завантаження довідників",
                    getErrorMessage(e)
            );
            return Optional.empty();
        }

        if (routes.isEmpty() || vehicles.isEmpty()) {
            showWarning(
                    "Для реєстрації інциденту потрібні " +
                            "маршрути та транспортні засоби."
            );
            return Optional.empty();
        }

        Dialog<Incident> dialog = new Dialog<>();

        dialog.setTitle("Реєстрація інциденту");
        dialog.setHeaderText(
                "Введення даних нового інциденту"
        );

        ButtonType saveType = new ButtonType(
                "Зареєструвати",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType,
                ButtonType.CANCEL
        );

        ComboBox<Route> routeCombo = new ComboBox<>(
                FXCollections.observableArrayList(routes)
        );

        ComboBox<Vehicle> vehicleCombo = new ComboBox<>(
                FXCollections.observableArrayList(vehicles)
        );

        routeCombo.setMaxWidth(Double.MAX_VALUE);
        vehicleCombo.setMaxWidth(Double.MAX_VALUE);

        vehicleCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(
                    Vehicle vehicle,
                    boolean empty
            ) {
                super.updateItem(vehicle, empty);

                setText(
                        empty || vehicle == null
                                ? null
                                : vehicle.getRegistrationNumber() +
                                " — " + vehicle.getModel()
                );
            }
        });

        vehicleCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    Vehicle vehicle,
                    boolean empty
            ) {
                super.updateItem(vehicle, empty);

                setText(
                        empty || vehicle == null
                                ? null
                                : vehicle.getRegistrationNumber() +
                                " — " + vehicle.getModel()
                );
            }
        });

        vehicleCombo.valueProperty().addListener(
                (obs, oldVehicle, newVehicle) -> {

                    if (newVehicle == null) {
                        return;
                    }

                    for (Route route : routes) {
                        if (route.getId() ==
                                newVehicle.getRouteId()) {

                            routeCombo.setValue(route);
                            break;
                        }
                    }
                }
        );

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText(
                "Опиши обставини інциденту"
        );
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        Label indicatorLabel = new Label(
                "Показник обчислюється сервером"
        );
        indicatorLabel.getStyleClass().add("indicator-label");

        GridPane grid = createFormGrid();

        grid.addRow(
                0,
                new Label("Транспорт:"),
                vehicleCombo
        );

        grid.addRow(
                1,
                new Label("Маршрут:"),
                routeCombo
        );

        grid.addRow(
                2,
                new Label("Опис інциденту:"),
                descriptionArea
        );

        grid.add(
                indicatorLabel,
                0,
                3,
                2,
                1
        );

        Label errorLabel = createErrorLabel();
        grid.add(errorLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        saveButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    try {

                        if (vehicleCombo.getValue() == null) {
                            throw new IllegalArgumentException(
                                    "Вибери транспорт."
                            );
                        }

                        if (routeCombo.getValue() == null) {
                            throw new IllegalArgumentException(
                                    "Вибери маршрут."
                            );
                        }

                        if (vehicleCombo.getValue().getRouteId() !=
                                routeCombo.getValue().getId()) {

                            throw new IllegalArgumentException(
                                    "Маршрут не відповідає транспорту."
                            );
                        }

                        if (descriptionArea.getText() == null ||
                                descriptionArea.getText().isBlank()) {

                            throw new IllegalArgumentException(
                                    "Введи опис інциденту."
                            );
                        }

                        errorLabel.setText("");

                    } catch (IllegalArgumentException e) {
                        errorLabel.setText(e.getMessage());
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(button -> {

            if (button != saveType) {
                return null;
            }

            Incident incident = new Incident();

            incident.setVehicleId(
                    vehicleCombo.getValue().getId()
            );

            incident.setRouteId(
                    routeCombo.getValue().getId()
            );

            incident.setDescription(
                    descriptionArea.getText().trim()
            );

            return incident;
        });

        return dialog.showAndWait();
    }

    private void deleteIncident() {

        Incident selected = incidentTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери інцидент.");
            return;
        }

        if (confirmDelete(
                "Видалити інцидент №" +
                        selected.getId() + "?"
        )) {
            runOperation(
                    "Видалення інциденту...",
                    () -> apiClient.deleteIncident(
                            selected.getId()
                    ),
                    this::loadIncidents
            );
        }
    }

    private void changeIncidentStatus() {

        Incident selected = incidentTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери інцидент.");
            return;
        }

        String nextStatus = switch (selected.getStatus()) {
            case "ПОВІДОМЛЕНО" -> "ПІДТВЕРДЖЕНО";
            case "ПІДТВЕРДЖЕНО" -> "РЕАГУВАННЯ";
            case "РЕАГУВАННЯ" -> "ЗАКРИТО";
            default -> null;
        };

        if (nextStatus == null) {
            showWarning(
                    "Інцидент уже закритий або має невідомий статус."
            );
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Зміна статусу");
        confirmation.setHeaderText(
                "Інцидент №" + selected.getId()
        );

        confirmation.setContentText(
                "Поточний статус: " +
                        selected.getStatus() +
                        "\nНовий статус: " +
                        nextStatus +
                        "\n\nПідтвердити перехід?"
        );

        if (confirmation.showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isPresent()) {

            runOperation(
                    "Зміна статусу...",
                    () -> apiClient.changeIncidentStatus(
                            selected.getId(),
                            nextStatus
                    ),
                    this::loadIncidents
            );
        }
    }

    // =====================================
    // CRUD ЗАВДАНЬ РЕАГУВАННЯ
    // =====================================

    private void addResponseTask() {

        showResponseTaskDialog(null).ifPresent(responseTask ->
                runOperation(
                        "Створення завдання...",
                        () -> apiClient.createTask(responseTask),
                        this::loadTasks
                )
        );
    }

    private void editResponseTask() {

        ResponseTask selected = taskTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери завдання.");
            return;
        }

        showResponseTaskDialog(selected).ifPresent(responseTask ->
                runOperation(
                        "Оновлення завдання...",
                        () -> apiClient.updateTask(responseTask),
                        this::loadTasks
                )
        );
    }

    private void deleteResponseTask() {

        ResponseTask selected = taskTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showWarning("Вибери завдання.");
            return;
        }

        if (confirmDelete(
                "Видалити завдання №" +
                        selected.getId() + "?"
        )) {
            runOperation(
                    "Видалення завдання...",
                    () -> apiClient.deleteTask(
                            selected.getId()
                    ),
                    this::loadTasks
            );
        }
    }

    private Optional<ResponseTask> showResponseTaskDialog(
            ResponseTask existing
    ) {

        List<Incident> incidents;

        try {
            incidents = apiClient.getIncidents();
        } catch (Exception e) {
            showError(
                    "Помилка завантаження інцидентів",
                    getErrorMessage(e)
            );
            return Optional.empty();
        }

        if (incidents.isEmpty()) {
            showWarning(
                    "Спочатку зареєструй інцидент."
            );
            return Optional.empty();
        }

        boolean editing = existing != null;

        Dialog<ResponseTask> dialog = new Dialog<>();

        dialog.setTitle(
                editing
                        ? "Редагування завдання"
                        : "Нове завдання реагування"
        );

        ButtonType saveType = new ButtonType(
                "Зберегти",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType,
                ButtonType.CANCEL
        );

        ComboBox<Incident> incidentCombo = new ComboBox<>(
                FXCollections.observableArrayList(incidents)
        );

        incidentCombo.setMaxWidth(Double.MAX_VALUE);

        incidentCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(
                    Incident incident,
                    boolean empty
            ) {
                super.updateItem(incident, empty);

                setText(
                        empty || incident == null
                                ? null
                                : "№" + incident.getId() +
                                " — " + incident.getDescription()
                );
            }
        });

        incidentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    Incident incident,
                    boolean empty
            ) {
                super.updateItem(incident, empty);

                setText(
                        empty || incident == null
                                ? null
                                : "№" + incident.getId() +
                                " — " + incident.getDescription()
                );
            }
        });

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        TextField responsibleField = new TextField();

        if (editing) {

            descriptionArea.setText(
                    existing.getTaskDescription()
            );

            responsibleField.setText(
                    existing.getResponsiblePerson()
            );

            for (Incident incident : incidents) {

                if (incident.getId() ==
                        existing.getIncidentId()) {

                    incidentCombo.setValue(incident);
                    break;
                }
            }
        }

        GridPane grid = createFormGrid();

        grid.addRow(
                0,
                new Label("Інцидент:"),
                incidentCombo
        );

        grid.addRow(
                1,
                new Label("Опис завдання:"),
                descriptionArea
        );

        grid.addRow(
                2,
                new Label("Відповідальний:"),
                responsibleField
        );

        Label errorLabel = createErrorLabel();
        grid.add(errorLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        saveButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    try {

                        if (incidentCombo.getValue() == null) {
                            throw new IllegalArgumentException(
                                    "Вибери інцидент."
                            );
                        }

                        if (descriptionArea.getText() == null ||
                                descriptionArea.getText().isBlank()) {

                            throw new IllegalArgumentException(
                                    "Введи опис завдання."
                            );
                        }

                        if (responsibleField.getText() == null ||
                                responsibleField.getText().isBlank()) {

                            throw new IllegalArgumentException(
                                    "Вкажи відповідального."
                            );
                        }

                        errorLabel.setText("");

                    } catch (IllegalArgumentException e) {
                        errorLabel.setText(e.getMessage());
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(button -> {

            if (button != saveType) {
                return null;
            }

            ResponseTask responseTask = new ResponseTask();

            responseTask.setId(
                    editing ? existing.getId() : 0
            );

            responseTask.setIncidentId(
                    incidentCombo.getValue().getId()
            );

            responseTask.setTaskDescription(
                    descriptionArea.getText().trim()
            );

            responseTask.setResponsiblePerson(
                    responsibleField.getText().trim()
            );

            return responseTask;
        });

        return dialog.showAndWait();
    }

    // =====================================
    // CSV-ІМПОРТ
    // =====================================

    private void importCsv(Stage stage) {

        if (importing || busy) {
            showWarning(
                    "Зачекай завершення поточної операції."
            );
            return;
        }

        int selectedTab = tabPane.getSelectionModel()
                .getSelectedIndex();

        if (selectedTab != 1 && selectedTab != 2) {
            showWarning(
                    "CSV-імпорт доступний для транспорту та інцидентів."
            );
            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle(
                selectedTab == 1
                        ? "Імпорт транспортних засобів"
                        : "Імпорт інцидентів"
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV-файли (*.csv)",
                        "*.csv"
                )
        );

        File file = chooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        CsvImporter importer = new CsvImporter(apiClient);

        Task<CsvImporter.ImportResult> task =
                selectedTab == 1
                        ? importer.createVehicleImportTask(
                        file.toPath()
                )
                        : importer.createIncidentImportTask(
                        file.toPath()
                );

        importing = true;
        updateButtons();

        importProgress.progressProperty().bind(
                task.progressProperty()
        );

        importStatusLabel.textProperty().bind(
                task.messageProperty()
        );

        importProgress.setVisible(true);
        importProgress.setManaged(true);

        importStatusLabel.setVisible(true);
        importStatusLabel.setManaged(true);

        task.setOnSucceeded(event -> {

            CsvImporter.ImportResult result = task.getValue();

            finishCsvImport();

            StringBuilder message = new StringBuilder();

            message.append("Усього рядків: ")
                    .append(result.total())
                    .append("\nУспішно: ")
                    .append(result.imported())
                    .append("\nПропущено: ")
                    .append(result.skipped());

            if (!result.errors().isEmpty()) {

                message.append("\n\nПомилки:\n");

                for (String error : result.errors()) {
                    message.append(error).append("\n");
                }
            }

            Alert alert = new Alert(
                    result.skipped() == 0
                            ? Alert.AlertType.INFORMATION
                            : Alert.AlertType.WARNING
            );

            alert.setTitle("Результат CSV-імпорту");
            alert.setHeaderText(
                    "Обробку CSV-файлу завершено"
            );

            TextArea details = new TextArea(
                    message.toString()
            );

            details.setEditable(false);
            details.setWrapText(true);
            details.setPrefWidth(500);
            details.setPrefHeight(250);

            alert.getDialogPane().setContent(details);
            alert.showAndWait();

            if (selectedTab == 1) {
                loadVehicles();
            } else {
                loadIncidents();
            }
        });

        task.setOnFailed(event -> {

            String error = getErrorMessage(
                    task.getException()
            );

            finishCsvImport();

            showError(
                    "Помилка CSV-імпорту",
                    error
            );

            if (selectedTab == 1) {
                loadVehicles();
            } else {
                loadIncidents();
            }
        });

        task.setOnCancelled(event -> {

            finishCsvImport();

            importStatusLabel.setText(
                    "Імпорт скасовано"
            );

            if (selectedTab == 1) {
                loadVehicles();
            } else {
                loadIncidents();
            }
        });

        startBackgroundTask(task, "csv-import");
    }

    private void finishCsvImport() {

        importing = false;

        importProgress.progressProperty().unbind();
        importStatusLabel.textProperty().unbind();

        updateButtons();
    }

    // =====================================
    // ДІАЛОГОВІ ВІКНА
    // =====================================

    private GridPane createFormGrid() {

        GridPane grid = new GridPane();

        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));

        ColumnConstraints labels = new ColumnConstraints();
        labels.setMinWidth(165);

        ColumnConstraints fields = new ColumnConstraints();
        fields.setMinWidth(300);

        grid.getColumnConstraints().addAll(
                labels,
                fields
        );

        return grid;
    }

    private Label createErrorLabel() {

        Label label = new Label();

        label.getStyleClass().add("error-label");
        label.setWrapText(true);

        return label;
    }

    private boolean confirmDelete(String message) {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Підтвердження видалення");
        alert.setHeaderText(message);
        alert.setContentText(
                "Цю дію неможливо скасувати."
        );

        return alert.showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isPresent();
    }

    // =====================================
    // ФОНОВІ HTTP-ОПЕРАЦІЇ
    // =====================================

    @FunctionalInterface
    private interface Request<T> {
        T execute() throws Exception;
    }

    private void runOperation(
            String message,
            Request<?> operation,
            Runnable afterSuccess
    ) {

        setBusy(true);
        statusLabel.setText(message);

        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {
                operation.execute();
                return null;
            }
        };

        task.setOnSucceeded(event -> {

            setBusy(false);
            statusLabel.setText(
                    "Операцію успішно виконано"
            );

            afterSuccess.run();
        });

        task.setOnFailed(event -> {

            setBusy(false);
            statusLabel.setText(
                    "Помилка виконання операції"
            );

            showError(
                    "Помилка збереження",
                    getErrorMessage(
                            task.getException()
                    )
            );
        });

        startBackgroundTask(
                task,
                "crud-operation"
        );
    }

    // =====================================
    // ЗАВАНТАЖЕННЯ ДАНИХ
    // =====================================

    private void refreshCurrentTab() {

        int index = tabPane.getSelectionModel()
                .getSelectedIndex();

        switch (index) {
            case 0 -> loadRoutes();
            case 1 -> loadVehicles();
            case 2 -> loadIncidents();
            case 3 -> loadTasks();
            default -> {
            }
        }
    }

    private void loadRoutes() {

        runRequest(
                "Завантаження маршрутів...",
                apiClient::getRoutes,
                routes -> routeTable.setItems(
                        FXCollections.observableArrayList(
                                routes
                        )
                )
        );
    }

    private void loadVehicles() {

        runRequest(
                "Завантаження транспорту...",
                apiClient::getVehicles,
                vehicles -> vehicleTable.setItems(
                        FXCollections.observableArrayList(
                                vehicles
                        )
                )
        );
    }

    private void loadIncidents() {

        runRequest(
                "Завантаження інцидентів...",
                apiClient::getIncidents,
                incidents -> incidentTable.setItems(
                        FXCollections.observableArrayList(
                                incidents
                        )
                )
        );
    }

    private void loadTasks() {

        runRequest(
                "Завантаження завдань...",
                apiClient::getTasks,
                tasks -> taskTable.setItems(
                        FXCollections.observableArrayList(
                                tasks
                        )
                )
        );
    }

    private <T> void runRequest(
            String message,
            Request<List<T>> request,
            Consumer<List<T>> onSuccess
    ) {

        setBusy(true);
        statusLabel.setText(message);

        Task<List<T>> task = new Task<>() {

            @Override
            protected List<T> call() throws Exception {
                return request.execute();
            }
        };

        task.setOnSucceeded(event -> {

            onSuccess.accept(
                    task.getValue()
            );

            statusLabel.setText(
                    "Дані завантажено: " +
                            task.getValue().size() +
                            " записів"
            );

            setBusy(false);
        });

        task.setOnFailed(event -> {

            statusLabel.setText(
                    "Помилка завантаження даних"
            );

            setBusy(false);

            showError(
                    "Помилка підключення",
                    getErrorMessage(
                            task.getException()
                    )
            );
        });

        startBackgroundTask(
                task,
                "api-loading"
        );
    }

    private void startBackgroundTask(
            Task<?> task,
            String name
    ) {

        Thread thread = new Thread(task, name);

        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean value) {

        busy = value;

        progress.setVisible(value);
        progress.setManaged(value);

        updateButtons();
    }

    private String getErrorMessage(
            Throwable error
    ) {

        if (error == null ||
                error.getMessage() == null) {

            return "Невідома помилка.";
        }

        return error.getMessage();
    }

    private void showWarning(String message) {

        Alert alert = new Alert(
                Alert.AlertType.WARNING
        );

        alert.setTitle("Увага");
        alert.setHeaderText("Перевір дані");
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showError(
            String title,
            String message
    ) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle(title);
        alert.setHeaderText(
                "Не вдалося виконати операцію"
        );
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
