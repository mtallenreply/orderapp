package lernen.orderapp.service;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(final String customerId) {
        super("Kunde mit ID " + customerId + " wurde nicht gefunden");
    }
}