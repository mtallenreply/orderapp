package lernen.orderapp.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class RestAPI {

    @PostMapping("/api/batch-jobs/order-import")
    public void orderImport(){
        //Startet einen Batch-Lauf für eine übergebene CSV-Datei
    }
    @GetMapping("/api/batch-jobs/order-import/{executionId}")
    public void getOrderImport( @PathVariable("executionId") @NotBlank  String executionId){
        //Liefert Status/Ergebnis eines Batch-Laufs
    }
    @GetMapping("/api/orders")
    public void getOrders(){
        //Bestellungen abfragen; Filter nach customerId, Channel, dateFrom/dateTo; Paging & Sortierung
    }
    @GetMapping("/api/customers/{customerId}/statistics")
    public void getStatistics(@PathVariable("customerId") @NotBlank  String customerId){
        //Aggregierte Kennzahlen für einen Kunden (Gesamtumsatz, Bestellanzahl)
    }
    @GetMapping("/api/statistics/top-customers?limit=5&dateFrom=...&dateTo=...")
    public void getTopCustomers(){
        //Top-Kunden nach Umsatz im Zeitraum
    }

}
