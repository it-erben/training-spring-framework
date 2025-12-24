package tech.erben.springboot.webdemo.api;

import jakarta.validation.constraints.*;
import tech.erben.springboot.webdemo.validation.AllowedCategory;
import tech.erben.springboot.webdemo.validation.OnCreate;
import tech.erben.springboot.webdemo.validation.OnUpdate;

import java.math.BigDecimal;

public class BookRequest {

    @NotBlank(groups = OnCreate.class)
    @Size(max = 120)
    private String title;

    @NotBlank(groups = OnCreate.class)
    @Size(max = 80)
    private String author;

    @AllowedCategory(groups = { OnCreate.class, OnUpdate.class })
    private String category;

    @NotNull(groups = OnCreate.class)
    @Positive(groups = { OnCreate.class, OnUpdate.class })
    private BigDecimal price;

    @NotNull(groups = OnCreate.class)
    @Min(value = 1950, groups = { OnCreate.class, OnUpdate.class })
    @Max(value = 2050, groups = { OnCreate.class, OnUpdate.class })
    private Integer publicationYear;

    @Pattern(
        regexp = "^[A-Z]{2}$",
        message = "Use ISO 3166-1 alpha-2 country codes",
        groups = { OnCreate.class, OnUpdate.class }
    )
    private String originCountry;

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
}
