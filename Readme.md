# Readme

## Start

Die OrderApp liest Zeilenweise CSV Dateien ein und legt sie in eine interne DB ab. Dies macht sie mit einem Spring Batch Verfahren.
Die Daten die übergeben werden sehen so aus:

Beispiel input Daten:

| orderId  |  customerId |customerName   | productSku  |quantity| unitPrice  |orderDate   | channel |
|---|---|---|---|---|---|---|---------|
| ORD-2001  | C-1001  | Anna Berger  | SKU-1001  | 5  | 19.99  | 2026-01-05  | ONLINE  |
...
## Steuerung des Programms:

- Bauen (kompiliert + führt Tests aus)
```Bash
./mvnw clean install
```
- Nur starten (ohne Tests)

```Bash
./mvnw spring-boot:run
```
- Nur Tests ausführen

```Bash
./mvnw test
```
## FAQ 

1. Wiederholbarkeit / Idempotenz: Ein erneuter Lauf des Jobs mit denselben Eingabedaten darf keine doppelten Datensätze erzeugen.
   Begründe deine gewählte Strategie (z.B. Job-Parameter, Unique-Constraints, Vorab-Prüfung) kurz im README.

Die Db an sich verhindert schon, dass dort Datensätze die gleiche OrderIds oder auch CustomerIDs haben angelegt werden. 
Allerdings würden Daten Verändert/Updated, wenn der Job Veränderte Daten (bis auf die IDs) verarbeiten würde.
Deswegen wird eine Prüfung irgendwo relevant oder die Strategie Wahl sollte zumindest festgehalten werden. Ich verlasse mich hier allein auf die Unique-Constraints der DB.

Man kann auf dieser Ebene es noch etwas anders gestalten:

   1. mit mehreren Spalten die zusammen als Primary Key definiert sind. Damit kann man dann z.B. order Id, quantity und productSku als gemeinsamen Primary Key nehmen. 
   Dies verhindert durch die Unique Constraint, dann dass doppelte Zeilendaten vorhanden sind.
   2. oder man baut noch einen echten Timestamp mit ein. Aber ein skaliertes System mit etlichen Bestellungen könnte dort auch an die Grenzen kommen und doch mal doppelte Timestamps ablegen wollen. 
   3. (Meine Empfehlung)Oder ganz auf die Erzeugung von einer ID von außen verzichten,
   dann würde man die ID selber generieren und man würde dann alle Bestellungen einzeln akzeptieren und ablegen.

Vorab Prüfung:
Man prüft immer vorher, ob die Order ID schon vorhanden ist, wenn ja, dann wird die Order nicht akzeptiert.
- Eine Vorab-Prüfung würde Performance kosten, da man die DB jeweils vorher immer Abfragen müsste. 
+ dies ist für Entwickler und für andere besser zu verstehen und weniger Fehler anfällig da es expliziter ist



