package lernen.orderapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import lernen.orderapp.service.OrderImportService;
import lernen.orderapp.service.StatisticsAggregator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
public class RestAPI {
    private final OrderImportService orderImportService;
    private final StatisticsAggregator statisticsAggregator;

    @Operation(summary = "Startet einen CSV Orderverarbeitungsjob", description = "Startet einen CSV Orderverarbeitungsjob")

    @PostMapping(value = "/api/batch-jobs/order-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long postOrderImport(@RequestParam("file") final MultipartFile file) throws IOException, JobInstanceAlreadyCompleteException, InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        final Path tempFile = Files.createTempFile("order-import-", ".csv");
        //Files.write(tempFile, file.getBytes()); // dies ist schlecht für sehr große Dateien wir wollen es lieber häppchenweise verarbeiten

        try (final InputStream in = file.getInputStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return orderImportService.fileImport(tempFile);
        //Startet einen Batch-Lauf und gibt eine ID zurück für eine übergebene CSV-Datei
    }
    @Operation(summary = "Execution Job Ergebnis abfragen", description = "liefert eine Map mit Errgebnissen")
    @GetMapping("/api/batch-jobs/order-import/{executionId}")
    public  ExitStatus getOrderImport( @PathVariable("executionId")  final Long executionId){
        //Liefert Status/Ergebnis eines Batch-Laufs
        return orderImportService.getOrderImportStatus(executionId);
    }
    @Operation(summary = "Bestellungen abfragen", description = "Filtert Bestellungen nach Kunde, Kanal und Zeitraum, optional sortiert nach Datum.")
    @GetMapping("/api/orders")
    public List<DTO.OrderResponse> getOrders(final DTO.OrderRequest request){
        //Bestellungen abfragen; Filter nach customerId, Channel, dateFrom/dateTo; Paging & Sortierung
        return  statisticsAggregator.getOrders(request.customerId(),request.channel(),request.dateFrom(),request.dateTo(),request.sorting())
        .stream().map(DTO.OrderResponse::from).toList();
    }

    @Operation(summary = "liefert Statistiken", description = "liefert Kundenstatistiken")
    @GetMapping("/api/customers/{customerId}/statistics")
    public Map<String, Object> getStatistics(@PathVariable("customerId") @NotBlank  final String customerId){
        //Aggregierte Kennzahlen für einen Kunden (Gesamtumsatz, Bestellanzahl)
        return  statisticsAggregator.getStatisticsOfCustomer(customerId);
    }
    @Operation(summary = "Liefert die besten Kunden", description = "Liefert die besten Kunden")
    @GetMapping("/api/statistics/top-customers")

    public List<String> getTopCustomers(@Parameter(description = "Maximale Anzahl zurückgegebener Kunden") @RequestParam("limit") final Long limit,
                                        @Parameter(description = "Start des Auswertungszeitraums (yyyy-MM-dd)") @RequestParam("dateFrom") final LocalDate dateFrom,
                                        @Parameter(description = "Ende des Auswertungszeitraums (yyyy-MM-dd)") @RequestParam("dateTo") final LocalDate dateTo){
        //Top-Kunden nach Umsatz im Zeitraum
        return statisticsAggregator.calcTop(dateFrom,dateTo,limit);
    }

}
