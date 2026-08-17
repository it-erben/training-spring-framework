/**
 * Modul 03 — Data und JPA: Ausgangszustand für das Live-Coding.
 *
 * <p>Nur die Anwendungsklasse und die Konfiguration sind da. Entities,
 * Repository, Service und Seed-Runner fehlen — sie entstehen live, die
 * Schrittnummern verweisen auf die README des Moduls.
 */
// TODO: Modul 03 — Schritt 1: Book-Entity anlegen (@Entity, @Id @GeneratedValue Long id, isbn, title, netPrice — Getter/Setter, geschützter No-Arg-Konstruktor)
// TODO: Modul 03 — Schritt 2: Author-Entity anlegen und Beziehung ziehen (@ManyToOne Author in Book, @OneToMany(mappedBy = "author") in Author)
// TODO: Modul 03 — Schritt 3: BookRepository (extends JpaRepository) und SeedDataRunner (CommandLineRunner, zwei Autoren, fünf Bücher) anlegen
// TODO: Modul 03 — Schritt 4: Derived Queries ergänzen (findByIsbn, findByTitleContainingIgnoreCase, findByNetPriceLessThan) und im Runner aufrufen
// TODO: Modul 03 — Schritt 5: JPQL-Query findByAuthorName mit @Query ergänzen und im Runner aufrufen
// TODO: Modul 03 — Schritt 6: BookService mit @Transactional raisePrices(factor) anlegen und im Runner aufrufen
// TODO: Modul 03 — Schritt 7: raisePricesAndFail(factor) ergänzen (flush, dann IllegalStateException) — die Rollback-Demo
package tech.erben.springboot.basics.data;
