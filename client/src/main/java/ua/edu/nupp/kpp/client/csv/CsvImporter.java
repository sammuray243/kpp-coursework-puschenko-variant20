
package ua.edu.nupp.kpp.client.csv;

import javafx.concurrent.Task;

import ua.edu.nupp.kpp.client.http.ApiClient;
import ua.edu.nupp.kpp.client.model.Incident;
import ua.edu.nupp.kpp.client.model.Vehicle;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvImporter {

    private static final Logger LOGGER =
            Logger.getLogger(CsvImporter.class.getName());

    private static final String INCIDENT_HEADER =
            "vehicleId,routeId,description";

    private static final String VEHICLE_HEADER =
            "registration_number,vehicle_type,model,route_id";

    private final ApiClient apiClient;

    public CsvImporter(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // Залишаємо старий метод для сумісності з MainApp.
    public Task<ImportResult> createImportTask(Path file) {
        return createIncidentImportTask(file);
    }

    public Task<ImportResult> createIncidentImportTask(Path file) {
        return createTask(file, false);
    }

    public Task<ImportResult> createVehicleImportTask(Path file) {
        return createTask(file, true);
    }

    private Task<ImportResult> createTask(
            Path file,
            boolean vehicleImport
    ) {

        return new Task<>() {

            @Override
            protected ImportResult call() throws Exception {

                if (!Files.isRegularFile(file)) {
                    throw new IOException(
                            "CSV-файл не знайдено: " + file
                    );
                }

                String expectedHeader = vehicleImport
                        ? VEHICLE_HEADER
                        : INCIDENT_HEADER;

                String typeName = vehicleImport
                        ? "транспортних засобів"
                        : "інцидентів";

                LOGGER.info(
                        "Початок CSV-імпорту " + typeName
                );

                updateMessage("Перевірка CSV-файлу...");

                List<String> rows = new ArrayList<>();

                try (BufferedReader reader =
                             Files.newBufferedReader(
                                     file,
                                     StandardCharsets.UTF_8
                             )) {

                    String header = reader.readLine();

                    if (header == null) {
                        throw new IOException(
                                "CSV-файл порожній."
                        );
                    }

                    header = header
                            .replace("\uFEFF", "")
                            .trim();

                    if (!expectedHeader.equals(header)) {
                        throw new IOException(
                                "Неправильний заголовок CSV. " +
                                        "Очікується: " + expectedHeader
                        );
                    }

                    String line;

                    while ((line = reader.readLine()) != null) {
                        rows.add(line);
                    }
                }

                int total = rows.size();
                int imported = 0;
                int skipped = 0;

                List<String> errors = new ArrayList<>();

                updateProgress(0, Math.max(total, 1));

                for (int i = 0; i < total; i++) {

                    if (isCancelled()) {
                        updateMessage("Імпорт скасовано.");
                        break;
                    }

                    int lineNumber = i + 2;
                    String line = rows.get(i);

                    updateMessage(
                            "Обробка рядка " + (i + 1) +
                                    " із " + total
                    );

                    try {

                        if (line.isBlank()) {
                            throw new IllegalArgumentException(
                                    "Порожній рядок."
                            );
                        }

                        String[] fields = line.split(",", -1);

                        if (vehicleImport) {

                            if (fields.length != 4) {
                                throw new IllegalArgumentException(
                                        "Очікується 4 поля, отримано " +
                                                fields.length
                                );
                            }

                            String registrationNumber =
                                    fields[0].trim();

                            String vehicleType =
                                    fields[1].trim();

                            String model =
                                    fields[2].trim();

                            int routeId = Integer.parseInt(
                                    fields[3].trim()
                            );

                            if (registrationNumber.isEmpty() ||
                                    vehicleType.isEmpty() ||
                                    model.isEmpty()) {
                                throw new IllegalArgumentException(
                                        "Текстові поля не можуть бути порожніми."
                                );
                            }

                            if (routeId <= 0) {
                                throw new IllegalArgumentException(
                                        "ID маршруту повинен бути додатним."
                                );
                            }

                            Vehicle vehicle = new Vehicle();

                            vehicle.setRegistrationNumber(
                                    registrationNumber
                            );
                            vehicle.setVehicleType(vehicleType);
                            vehicle.setModel(model);
                            vehicle.setRouteId(routeId);

                            apiClient.createVehicle(vehicle);

                        } else {

                            if (fields.length != 3) {
                                throw new IllegalArgumentException(
                                        "Очікується 3 поля, отримано " +
                                                fields.length
                                );
                            }

                            int vehicleId = Integer.parseInt(
                                    fields[0].trim()
                            );

                            int routeId = Integer.parseInt(
                                    fields[1].trim()
                            );

                            String description = fields[2].trim();

                            if (vehicleId <= 0 || routeId <= 0) {
                                throw new IllegalArgumentException(
                                        "ID повинні бути додатними."
                                );
                            }

                            if (description.isEmpty()) {
                                throw new IllegalArgumentException(
                                        "Опис інциденту порожній."
                                );
                            }

                            Incident incident = new Incident();

                            incident.setVehicleId(vehicleId);
                            incident.setRouteId(routeId);
                            incident.setDescription(description);

                            apiClient.createIncident(incident);
                        }

                        imported++;

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                        throw e;

                    } catch (Exception e) {

                        skipped++;

                        String error =
                                "Рядок " + lineNumber +
                                        ": " + e.getMessage();

                        errors.add(error);

                        LOGGER.log(
                                Level.WARNING,
                                "Помилка імпорту: " + error
                        );
                    }

                    updateProgress(i + 1, Math.max(total, 1));
                }

                LOGGER.info(
                        "CSV-імпорт завершено. Тип: " + typeName +
                                ", успішно: " + imported +
                                ", пропущено: " + skipped
                );

                updateMessage(
                        "Завершено. Успішно: " +
                                imported + ", пропущено: " + skipped
                );

                return new ImportResult(
                        total,
                        imported,
                        skipped,
                        errors
                );
            }
        };
    }

    public record ImportResult(
            int total,
            int imported,
            int skipped,
            List<String> errors
    ) {
    }
}
