-- Testdaten fuer CourseRepositoryTest: zwei Trainer, vier Kurse.
-- Zwei Kurse mit "Spring" im Titel, zwei mit mehr als 10 Plaetzen,
-- zwei mit einer Netto-Gebuehr zwischen 1400 und 1700 Euro.
insert into trainer (id, name, email) values (1, 'Erika Muster', 'erika.muster@example.org');
insert into trainer (id, name, email) values (2, 'Max Beispiel', 'max.beispiel@example.org');

insert into course (id, code, title, seats, net_fee, trainer_id)
values (1, 'SB-ADV', 'Spring Boot Advanced', 12, 1650.00, 1);
insert into course (id, code, title, seats, net_fee, trainer_id)
values (2, 'SB-BAS', 'Spring Boot Basis', 8, 1450.00, 1);
insert into course (id, code, title, seats, net_fee, trainer_id)
values (3, 'JAVA-MOD', 'Modernes Java', 15, 1800.00, 2);
insert into course (id, code, title, seats, net_fee, trainer_id)
values (4, 'K8S-GRD', 'Kubernetes Grundlagen', 10, 1250.00, 2);
