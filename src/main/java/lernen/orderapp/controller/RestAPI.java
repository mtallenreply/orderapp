package lernen.orderapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lernen.orderapp.entity.Order;
import lernen.orderapp.service.OrderImportService;
import lernen.orderapp.service.StatisticsAggregator;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
    public ResponseEntity<Long> postOrderImport(@RequestParam("file") final MultipartFile file) throws IOException, JobInstanceAlreadyCompleteException, InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        final Path tempFile = Files.createTempFile("order-import-", ".csv");

        try (final InputStream in = file.getInputStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        final Long executionId = orderImportService.fileImport(tempFile);
        return ResponseEntity.created(URI.create("/api/batch-jobs/order-import/" + executionId))
                .body(executionId);
    }
    @Operation(summary = "Execution Job Ergebnis abfragen", description = "liefert eine Map mit Ergebnissen")
    @GetMapping("/api/batch-jobs/order-import/{executionId}")
    public  ResponseEntity<ExitStatus> getOrderImport( @PathVariable("executionId")  final Long executionId){

        return ResponseEntity.ok( orderImportService.getOrderImportStatus(executionId));
    }
    @Operation(summary = "Bestellungen abfragen", description = "Filtert Bestellungen nach Kunde, Kanal und Zeitraum, optional sortiert nach Datum.")
    @GetMapping("/api/orders")
    public ResponseEntity<Page<DTO.OrderResponse>> getOrders(@Valid @ParameterObject final DTO.OrderRequest request, final Pageable pageable){
        final Page<Order> orderPage = statisticsAggregator.getOrders(
                request.customerId(), request.channel(), request.dateFrom(), request.dateTo(), pageable);
        final Page<DTO.OrderResponse> response = orderPage.map(DTO.OrderResponse::from);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "liefert Statistiken", description = "liefert Kundenstatistiken")
    @GetMapping("/api/customers/{customerId}/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable("customerId") @NotBlank  final String customerId){
        return  ResponseEntity.ok(statisticsAggregator.getStatisticsOfCustomer(customerId));
    }
    @Operation(summary = "Liefert die besten Kunden", description = "Liefert die besten Kunden")
    @GetMapping("/api/statistics/top-customers")

    public ResponseEntity<List<StatisticsAggregator.TopCustomer>> getTopCustomers(@Parameter(description = "Maximale Anzahl zurückgegebener Kunden") @RequestParam("limit") final Long limit,
                                                                                  @Parameter(description = "Start des Auswertungszeitraums (yyyy-MM-dd)") @RequestParam("dateFrom") final LocalDate dateFrom,
                                                                                  @Parameter(description = "Ende des Auswertungszeitraums (yyyy-MM-dd)") @RequestParam("dateTo") final LocalDate dateTo){
        return ResponseEntity.ok(statisticsAggregator.calcTop(dateFrom,dateTo,limit));
    }

}
