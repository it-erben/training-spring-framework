package tech.erben.springboot.batch;

public record OrderLine(String orderId, String title, int quantity, int unitPriceCents) {

    public int totalCents() {
        return quantity * unitPriceCents;
    }
}
