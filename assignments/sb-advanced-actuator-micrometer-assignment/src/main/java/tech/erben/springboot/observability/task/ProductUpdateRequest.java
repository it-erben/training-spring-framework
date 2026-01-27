package tech.erben.springboot.observability.task;

import java.math.BigDecimal;

public class ProductUpdateRequest {

    private String name;
    private BigDecimal price;
    private int inventory;

    public ProductUpdateRequest() {
    }

    public ProductUpdateRequest(String name, BigDecimal price, int inventory) {
        this.name = name;
        this.price = price;
        this.inventory = inventory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }
}
