-- Testdaten für BookRepositoryTest: zwei Autoren, drei Bücher.
-- Zwei Bücher von Joshua Bloch mit "Java" im Titel, davon eines unter 40 Euro.
insert into author (id, name) values (1, 'Joshua Bloch');
insert into author (id, name) values (2, 'Eric Evans');

insert into book (id, isbn, title, net_price, author_id)
values (1, '978-0-13-468599-1', 'Effective Java', 44.99, 1);
insert into book (id, isbn, title, net_price, author_id)
values (2, '978-0-321-33678-1', 'Java Puzzlers', 35.00, 1);
insert into book (id, isbn, title, net_price, author_id)
values (3, '978-0-321-12521-7', 'Domain-Driven Design', 52.30, 2);
