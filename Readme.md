# Readme

## Start

Starte die App

```Bash
# Bauen (kompiliert + führt Tests aus)
./mvnw clean install
```
# Nur starten (ohne Tests)

```Bash
./mvnw spring-boot:run
```
# Nur Tests ausführen

```Bash
./mvnw test
```
## FAQ 

1. Wiederholbarkeit / Idempotenz: Ein erneuter Lauf des Jobs mit denselben Eingabedaten darf keine doppelten Datensätze erzeugen.
   Begründe deine gewählte Strategie (z.B. Job-Parameter, Unique-Constraints, Vorab-Prüfung) kurz im README.

Die Db an sich verhindert schon, dass dort Datensätze die gleiche OrderIds oder auch CustomerIDs haben angelegt werden. 
Allerdings würden Daten Verändert/Updated, wenn der Job Veränderte Daten (bis auf die IDs) verarbeiten würde.
Deswegen wird eine Prüfung irgendwo relevant oder die Strategie Wahl sollte zumindest festgehalten werden. Ich verlasse mich hier allein auf die Unique-Constraints der DB.

2.