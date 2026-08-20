package tech.erben.springboot.batch;

public class PricingUnavailableException extends RuntimeException {

    public PricingUnavailableException(String message) {
        super(message);
    }
}
