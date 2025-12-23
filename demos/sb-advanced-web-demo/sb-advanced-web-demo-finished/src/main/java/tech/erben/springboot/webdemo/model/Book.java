package tech.erben.springboot.webdemo.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Book {

    private Long id;
    private String title;
    private String author;
    private String category;
    private BigDecimal price;
    private Integer publicationYear;
    private String originCountry;
    private Instant createdAt;

    public Book() {}

    public Book(
        Long id,
        String title,
        String author,
        String category,
        BigDecimal price,
        Integer publicationYear,
        String originCountry,
        Instant createdAt
    ) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.price = price;
        this.publicationYear = publicationYear;
        this.originCountry = originCountry;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
