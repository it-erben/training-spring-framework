package tech.erben.springboot.batch.operations;

public record OrderLine(String orderId, String title, int quantity, int unitPriceCents) {
}
