/**
 * Modul 03 — Data und JPA: Ausgangszustand fuer das Live-Coding.
 *
 * <p>Nur die Anwendungsklasse und die Konfiguration sind da. Entities,
 * Repository, Service und Seed-Runner fehlen — sie entstehen live, die
 * Schrittnummern verweisen auf die README des Moduls.
 */
// TODO: Modul 03 — Schritt 1: Book-Entity anlegen (@Entity, @Id @GeneratedValue Long id, isbn, title, netPrice — Getter/Setter, geschuetzter No-Arg-Konstruktor)
// TODO: Modul 03 — Schritt 2: Author-Entity anlegen und Beziehung ziehen (@ManyToOne Author in Book, @OneToMany(mappedBy = "author") in Author)
// TODO: Modul 03 — Schritt 3: BookRepository (extends JpaRepository) und SeedDataRunner (CommandLineRunner, zwei Autoren, fuenf Buecher) anlegen
// TODO: Modul 03 — Schritt 4: Derived Queries ergaenzen (findByIsbn, findByTitleContainingIgnoreCase, findByNetPriceLessThan) und im Runner aufrufen
// TODO: Modul 03 — Schritt 5: JPQL-Query findByAuthorName mit @Query ergaenzen und im Runner aufrufen
// TODO: Modul 03 — Schritt 6: BookService mit @Transactional raisePrices(factor) anlegen und im Runner aufrufen
// TODO: Modul 03 — Schritt 7: raisePricesAndFail(factor) ergaenzen (flush, dann IllegalStateException) — die Rollback-Demo
package tech.erben.springboot.basics.data;
