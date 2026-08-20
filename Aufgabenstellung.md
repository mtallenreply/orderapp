# Coding Challenge: Auftragsverarbeitung (Backend)

## 1. Kontext & Szenario

Ein fiktives Handelsunternehmen erhält täglich Bestelldateien aus mehreren Vertriebskanälen (Online-Shop, stationärer Handel, Vertriebspartner). Diese Dateien müssen eingelesen, fachlich angereichert (Rabattberechnung, Kundendaten) und aggregiert werden. Anschließend sollen die verarbeiteten Daten über eine REST-API abrufbar sein.

Deine Aufgabe ist es, einen Backend-Service zu entwickeln, der:

1. Bestelldaten aus einer CSV-Datei per **Spring Batch** importiert, anreichert und persistiert,
2. die verarbeiteten Daten und daraus abgeleitete Kennzahlen über eine **REST-API** zugänglich macht.

Es wird **keine eigene UI** benötigt – der Fokus liegt vollständig auf dem Backend.

## 2. Technischer Rahmen

- **Sprache**: Java 26
- **Framework**: Spring Boot 4.1.0
- **Build-Tool**: Maven
- **Persistenz**: H2 (In-Memory), Spring Data JPA
- **Batch**: Spring Batch
- **API-Dokumentation**: springdoc-openapi (Swagger UI)

Die genauen Patch-Versionen der Dependencies darfst du selbst wählen, sofern sie mit den oben genannten Hauptversionen kompatibel sind.

## 3. Eingabedaten

### 3.1 Bestelldaten (`beispiel-bestellungen.csv`)

Spalten:

| Spalte | Typ | Beschreibung |
|---|---|---|
| `orderId` | String | Eindeutige Bestell-ID |
| `customerId` | String | Kunden-ID |
| `customerName` | String | Anzeigename des Kunden |
| `productSku` | String | Artikelnummer |
| `quantity` | Integer | Bestellmenge |
| `unitPrice` | Decimal | Einzelpreis (netto) |
| `orderDate` | Date (`YYYY-MM-DD`) | Bestelldatum |
| `Channel` | String | `ONLINE`, `RETAIL` oder `PARTNER` |

Die beigefügte Datei ist bewusst fehlerfrei – der Fokus dieser Aufgabe liegt nicht auf Fehlertoleranz beim Parsen, sondern auf korrekter fachlicher Verarbeitung, Wiederholbarkeit und Nebenläufigkeit.

### 3.2 Kundenstammdaten

Zusätzlich zu den Bestelldaten liegen Kundenstammdaten vor (lege sie z.B. als zweite CSV oder als vordefinierte Tabelle/Seed-Daten in deinem Projekt an):

| Spalte | Typ | Beschreibung |
|---|---|---|
| `customerId` | String | Kunden-ID |
| `customerType` | String | `STANDARD`, `PREMIUM` oder `VIP` |
| `loyaltyDiscountPercent` | Decimal (optional) | Individueller Treuerabatt in Prozent – **nicht bei jedem Kunden gesetzt** |

Beispielhafte Werte:

```
customerId,customerType,loyaltyDiscountPercent
C-1001,VIP,7.5
C-1002,STANDARD,
C-1003,PREMIUM,3.0
C-1004,STANDARD,
C-1005,VIP,10.0
```

## 4. Fachliche Anforderungen an den Batch-Job

1. **Import**: Lies die Bestelldaten zeilenweise ein (Chunk-orientierte Verarbeitung).
2. **Anreicherung pro Bestellposition**:
   - Ordne die Kundenstammdaten der jeweiligen Bestellung zu. Da nicht jeder Kunde einen Treuerabatt hinterlegt hat, muss dies über die `Optional`-API sauber modelliert werden (kein `null`-Handling, keine ungeprüften `Optional.get()`-Aufrufe).
   - Berechne den Rabatt pro Position nach folgenden Regeln, implementiert als komponierbare Lambda-Ausdrücke (z.B. `Function<OrderLine, BigDecimal>` bzw. `Predicate`-Kombinationen):
     - **Mengenrabatt**: ab 10 Einheiten 5%, ab 50 Einheiten 10%
     - **Kanalrabatt**: `PARTNER`-Bestellungen erhalten zusätzlich 3%
     - **Treuerabatt**: falls beim Kunden hinterlegt, zusätzlich anwenden
     - Rabatte sind kombinierbar, aber der **Gesamtrabatt pro Position ist auf maximal 20% gedeckelt**
   - Berechne den Nettobetrag der Position nach Rabatt.
3. **Persistierung**: Speichere die angereicherten Bestellungen (inkl. berechnetem Rabatt und Nettobetrag) in der Datenbank.
4. **Wiederholbarkeit / Idempotenz**: Ein erneuter Lauf des Jobs mit denselben Eingabedaten darf **keine doppelten Datensätze** erzeugen. Begründe deine gewählte Strategie (z.B. Job-Parameter, Unique-Constraints, Vorab-Prüfung) kurz im README.
5. **Nebenläufigkeit**: Die Verarbeitung der Bestellzeilen soll **parallelisiert** erfolgen (z.B. Spring Batch Partitioning oder ein paralleler Step mit `TaskExecutor`). Die Wahl deines Ansatzes ist zu begründen.
6. **Aggregation**: Nach erfolgreichem Lauf müssen folgende Kennzahlen ableitbar sein (Berechnung z.B. beim Abruf über die API mittels Streams/Collectors, oder als eigener Aggregations-Step – deine Wahl):
   - Gesamtumsatz (nach Rabatt) pro Kunde
   - Anzahl Bestellungen pro Kanal
   - Top-5-Kunden nach Umsatz in einem gegebenen Zeitraum

## 5. Anforderungen an die REST-API

| Methode & Pfad | Beschreibung |
|---|---|
| `POST /api/batch-jobs/order-import` | Startet einen Batch-Lauf für eine übergebene CSV-Datei |
| `GET /api/batch-jobs/order-import/{executionId}` | Liefert Status/Ergebnis eines Batch-Laufs |
| `GET /api/orders` | Bestellungen abfragen; Filter nach `customerId`, `Channel`, `dateFrom`/`dateTo`; Paging & Sortierung |
| `GET /api/customers/{customerId}/statistics` | Aggregierte Kennzahlen für einen Kunden (Gesamtumsatz, Bestellanzahl) |
| `GET /api/statistics/top-customers?limit=5&dateFrom=...&dateTo=...` | Top-Kunden nach Umsatz im Zeitraum |

Zusätzliche Anforderungen:

- **Fehlerbehandlung**: Einheitliche, aussagekräftige Fehlerantworten nach RFC7807 (`ProblemDetail`, z.B. via `@ControllerAdvice`/`ResponseEntityExceptionHandler`). Fachliche Fehlerfälle (z.B. unbekannter Kunde, unbekannter Batch-Lauf) sollen über eigene Exceptions abgebildet werden, nicht über generische 500er.
- **API-Dokumentation**: Alle Endpoints müssen über springdoc-openapi dokumentiert und über Swagger-UI erreichbar sein.

## 6. Nicht-funktionale Anforderungen

- **Tests sind verpflichtend**: mindestens aussagekräftige Unit-Tests für die Rabattlogik/Aggregation sowie mindestens ein Integrationstest, der entweder den vollständigen Batch-Lauf oder einen REST-Endpoint End-to-End abdeckt.
- Saubere Schichtenarchitektur (z.B. Controller / Service / Repository / Batch-Konfiguration).
- Konsistente, idiomatische Verwendung der `Optional`-API (kein `Optional` als Feldtyp oder Methodenparameter, kein ungeprüftes `.get()`).
- Sinnvoller, aber nicht erzwungener Einsatz von Lambdas/Streams – Lesbarkeit geht vor "Stream um jeden Preis".

## 7. Abgabe

- Abgabe als **Git-Repository** (z.B. Zip mit `.git`-Historie oder Link zu einem Repo).
- **README.md** mit:
  - Anleitung zum Bauen, Starten und Testen (`mvn ...`-Befehle)
  - Kurzer Begründung der wichtigsten Design-Entscheidungen, insbesondere:
    - gewählte Idempotenz-/Wiederholbarkeits-Strategie
    - gewählter Ansatz zur Parallelisierung im Batch-Job
- Die beigefügte Datei `beispiel-bestellungen.csv` dient als Testdatensatz; du kannst bei Bedarf eigene weitere Testdaten ergänzen.

## 8. Zeitrahmen

Für diese Aufgabe solltest du als erfahrene:r Java-Entwickler:in **ca. 1–2 Tage** einplanen. Es ist völlig in Ordnung, sinnvolle Vereinfachungen zu treffen, sofern du sie im README kurz begründest.

Viel Erfolg!
