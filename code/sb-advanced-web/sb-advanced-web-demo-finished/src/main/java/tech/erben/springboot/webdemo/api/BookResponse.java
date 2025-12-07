package tech.erben.springboot.webdemo.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.math.BigDecimal;
import java.time.Instant;

@JacksonXmlRootElement(localName = "book")
public record BookResponse(
    Long id,
    String title,
    String author,
    String category,
    BigDecimal price,
    Integer publicationYear,
    String originCountry,
    Instant createdAt
) {}
