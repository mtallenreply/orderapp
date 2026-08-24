package lernen.orderapp.service;

public class JobExecutionNotFoundException extends RuntimeException {
    public JobExecutionNotFoundException(final Long executionId) {
        super("Batch-Lauf mit ID " + executionId + " wurde nicht gefunden");
    }
}