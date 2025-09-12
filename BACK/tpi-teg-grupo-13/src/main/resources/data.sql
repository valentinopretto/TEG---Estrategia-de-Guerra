-- src/main/resources/data.sql
-- Inicialización de datos para TEG
use teg;
-- Insertar continentes
INSERT INTO continents (name, bonus_armies) VALUES
                                                ('América del Sur', 3),
                                                ('América del Norte', 5),
                                                ('África', 3),
                                                ('Europa', 5),
                                                ('Asia', 7),
                                                ('Oceanía', 2);

-- Insertar países por continente
-- América del Sur (ID_CONTINENTE = 1)
INSERT INTO countries (name, continent_id) VALUES ('ARGENTINA', 1);
INSERT INTO countries (name, continent_id) VALUES ('BRASIL', 1);
INSERT INTO countries (name, continent_id) VALUES ('CHILE', 1);
INSERT INTO countries (name, continent_id) VALUES ('URUGUAY', 1);
INSERT INTO countries (name, continent_id) VALUES ('PERU', 1);
INSERT INTO countries (name, continent_id) VALUES ('COLOMBIA', 1);

-- América del Norte (ID_CONTINENTE = 2)
INSERT INTO countries (name, continent_id) VALUES ('MEXICO', 2);
INSERT INTO countries (name, continent_id) VALUES ('CALIFORNIA', 2);
INSERT INTO countries (name, continent_id) VALUES ('OREGON', 2);
INSERT INTO countries (name, continent_id) VALUES ('NUEVA YORK', 2);
INSERT INTO countries (name, continent_id) VALUES ('TERRANOVA', 2);
INSERT INTO countries (name, continent_id) VALUES ('LABRADOR', 2);
INSERT INTO countries (name, continent_id) VALUES ('GROENLANDIA', 2);
INSERT INTO countries (name, continent_id) VALUES ('CANADA', 2);
INSERT INTO countries (name, continent_id) VALUES ('ALASKA', 2);
INSERT INTO countries (name, continent_id) VALUES ('YUKOM', 2);



-- África (ID_CONTINENTE = 3)
INSERT INTO countries (name, continent_id) VALUES ('SAHARA', 3);
INSERT INTO countries (name, continent_id) VALUES ('ETIOPIA', 3);
INSERT INTO countries (name, continent_id) VALUES ('EGIPTO', 3);
INSERT INTO countries (name, continent_id) VALUES ('ZAIRE', 3);
INSERT INTO countries (name, continent_id) VALUES ('SUDAFRICA', 3);
INSERT INTO countries (name, continent_id) VALUES ('MADAGASCAR', 3);

-- Europa (ID_CONTINENTE = 4)
INSERT INTO countries (name, continent_id) VALUES ('ESPAÑA', 4);
INSERT INTO countries (name, continent_id) VALUES ('ISLANDIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('FRANCIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('ITALIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('ALEMANIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('POLONIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('RUSIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('SUECIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('GRAN BRETAÑA', 4);


-- Asia (ID_CONTINENTE = 5)
INSERT INTO countries (name, continent_id) VALUES ('TURQUIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('ISRAEL', 5);
INSERT INTO countries (name, continent_id) VALUES ('ARABIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('IRAN', 5);
INSERT INTO countries (name, continent_id) VALUES ('INDIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('GOBI', 5);
INSERT INTO countries (name, continent_id) VALUES ('MONGOLIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('CHINA', 5);
INSERT INTO countries (name, continent_id) VALUES ('JAPON', 5);
INSERT INTO countries (name, continent_id) VALUES ('SIBERIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('ARAL', 5);
INSERT INTO countries (name, continent_id) VALUES ('TARTARIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('TAYMIR', 5);
INSERT INTO countries (name, continent_id) VALUES ('KAMCHATKA', 5);
INSERT INTO countries (name, continent_id) VALUES ('MALASIA', 5);

-- Oceanía (ID_CONTINENTE = 6)
INSERT INTO countries (name, continent_id) VALUES ('JAVA', 6);
INSERT INTO countries (name, continent_id) VALUES ('AUSTRALIA', 6);
INSERT INTO countries (name, continent_id) VALUES ('SUMATRA', 6);
INSERT INTO countries (name, continent_id) VALUES ('BORNEO', 6);

-- Insertar fronteras (relaciones de vecindad
-- ARGENTINA (ID 1)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 3); -- CHILE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 5); -- PERU
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 4); -- URUGUAY
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 2); -- BRASIL

-- CHILE (ID 3)

INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (3, 1); -- ARGENTINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (3, 5);  -- PERU
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (3, 48); -- AUSTRALIA

-- URUGUAY (ID 4)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (4, 2); -- BRASIL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (4, 1); -- ARGENTINA

-- PERU (ID 5)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (5, 6); -- COLOMBIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (5, 3); -- CHILE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (5, 2); -- BRASIL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (5, 1); -- ARGENTINA

-- COLOMBIA (ID 6)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (6, 7); -- MEXICO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (6, 5); -- PERU
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (6, 2); -- BRASIL

-- BRASIL (ID 2)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (2, 17); -- SAHARA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (2, 1); -- ARGENTINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (2, 5); -- PERU
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (2, 4); -- URUGUAY
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (2, 6); -- COLOMBIA

-- MEXICO (7)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (7, 8); -- CALIFORNIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (7, 6); -- COLOMBIA

-- CALIFORNIA (8)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (8, 7); -- MEXICO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (8, 9); -- OREGON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (8, 10); -- NUEVA YORK

-- OREGON (9)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (9, 8); -- CALIFORNIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (9, 10); -- NUEVA YORK
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (9, 15); -- ALASKA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (9, 16); -- YUKON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (9, 14); -- CANADA

-- ALASKA (15)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (15, 9); -- OREGON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (15, 44); -- KAMCHATKA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (15, 16); -- YUKON

-- YUKON (16)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (16, 14); -- CANADA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (16, 9); -- OREGON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (16, 15); -- ALASKA

-- CANADA (14)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (14, 10); -- NUEVA YORK
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (14, 11); -- TERRANOVA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (14, 16); -- YUKON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (14, 15); -- ALASKA

-- NUEVA YORK (10)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (10, 13); -- GROENLANDIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (10, 9); -- OREGON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (10, 8); -- CALIFORNIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (10, 14); -- CANADA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (10, 11); -- TERRANOVA

-- TERRANOVA (11)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (11, 12); -- LABRADOR
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (11, 14); -- CANADA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (11, 10); -- NUEVA YORK

-- GROENLANDIA (13)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 12); -- LABRADOR
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 10); -- NUEVA YORK
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 24); -- ISLANDIA

-- Europa
--ISLANDIA (24)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (24, 31); -- ISLANDIA - GRAN BRETAÑA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (24, 13); -- ISLANDIA - GROENLANDIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (24, 30); -- ISLANDIA - SUECIA

--SUECIA (30)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (30, 29); -- SUECIA - RUSIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (30, 24); -- SUECIA - ISLANDIA

--GB (31)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (31, 23); -- GRAN BRETAÑA - ESPAÑA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (31, 27); -- GRAN BRETAÑA - ALEMANIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (31, 24); -- GRAN BRETAÑA - ISLANDIA

--ESP (23)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (23, 25); -- ESPAÑA - FRANCIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (23, 17); -- ESPAÑA - SAHARA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (23, 31); -- ESPAÑA - GRAN BRETAÑA

--FRC (25)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (25, 27); -- FRANCIA - ALEMANIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (25, 23); -- FRANCIA - ESP
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (25, 26); -- FRANCIA - ITALIA

--ITALIA (26)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (26, 27); -- ITALIA - ALEMANIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (26, 25); -- ITALIA - FRANCIA

-- GERMANY (27)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (27, 28); -- ALEMANIA - POLONIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (27, 26); -- ALEMANIA - ITALIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (27, 25); -- ALEMANIA - FRANCIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (27, 31); -- ALEMANIA - GRAN BRETAÑA

-- POLONIA (28)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 29); -- POLONIA - RUSIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 27); -- POLONIA - ALEMANIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 19); -- POLONIA - EGIPTO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 32); -- POLONIA - TURQUIA

-- RUSIA (29)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 42); -- RUSIA - ARAL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 35); -- RUSIA - IRAN
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 32); -- RUSIA - TURQUIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 30); -- RUSIA - SUECIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 28); -- RUSIA - POLONIA

-- África
-- SAHARA (17)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 2);  -- SAHARA - BRASIL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 19); -- SAHARA - EGIPTO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 18); -- SAHARA - ETIOPIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 20); -- SAHARA - ZAIRE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 23); -- SAHARA - ESPAÑA

-- EGIPTO (19)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 18); -- EGIPTO - ETIOPIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 17); -- EGIPTO - SAHARA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 28); -- EGIPTO - POLONIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 22); -- EGIPTO - MADAGASCAR
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 32); -- EGIPTO - TURQUIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 33); -- EGIPTO - ISRAEL

-- ETIOPIA (18)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 20); -- ETIOPIA - ZAIRE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 21); -- ETIOPIA - SUDAFRICA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 17); -- ETIOPIA - SAHARA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 19); -- ETIOPIA - EGIPTO

-- ZAIRE (19)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 17); -- ZAIRE - SAHARA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 18); -- ZAIRE - ETIOPIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 21); -- ZAIRE - SUDAFRICA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 22); -- ZAIRE - MADAGASCAR

--SUDAFRICA (21)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (21,19);  -- SUDAFRICA --ZAIRE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (21,18);  -- SUDAFRICA --ETIOPIA

-- MADAGASCAR (22)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (22,19);  -- MADAGASCAR --ZAIRE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (22,20);  -- MADAGASCAR --EGIPTO


-- Asia
-- TARTARIA (43)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (43, 42); -- TARTARIA - ARAL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (43, 44); -- TARTARIA - TAYMIR
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (43, 41); -- TARTARIA - SIBERIA

--ABAL (42)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 35); -- ARAL - IRAN
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 41); -- ARAL - SIBERIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 38); -- ARAL - MONGOLIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 29); -- ARAL - RUSIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 43); -- ARAL - TARTARIA

--TAYMINR(44)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (44, 41); -- TAYMIR - SIBERIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (44, 43); -- TAYMIR - TARTARIA

--SIBERIA(41)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 38); -- SIBERIA - MONGOLIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 39); -- SIBERIA - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 43); -- SIBERIA - TARTARIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 44); -- SIBERIA - TAYMIR
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 45); -- SIBERIA - KAMCHATKA

--KAMCHATKA(45)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 39); -- KAMCHATKA - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 41); -- KAMCHATKA - SIBERIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 40); -- KAMCHATKA - JAPON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 15); -- KAMCHATKA - ALASKA

--JAPON (40)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (40, 39); -- JAPON - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (40, 45); -- JAPON - KAMCHATKA

--MONGOLIA(38)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 39); -- MONGOLIA - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 41); -- MONGOLIA - SIBERIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 42); -- MONGOLIA - ABAL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 37); -- MONGOLIA - GOBI
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 35); -- MONGOLIA - IRAN

--CHINA(39)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 37); -- CHINA - GOBI
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 35); -- CHINA - IRAN
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 36); -- CHINA - INDIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 46); -- CHINA - MALASIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 40); -- CHINA - JAPON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 38); -- CHINA - MONGOLIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 45); -- CHINA - KAMCHATKA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 41); -- CHINA - SIBERIA

--GOBI(37)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (37, 35); -- GOBI - IRAN
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (37, 38); -- GOBI - MONGOLIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (37, 39); -- GOBI - CHINA

--IRAN(35)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 32); -- IRAN - TURQUIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 36); -- IRAN - INDIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 39); -- IRAN - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 38); -- IRAN - MONGOLIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 42); -- IRAN - ABAL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 29); -- IRAN - RUSIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 37); -- IRAN - GOBI

--TURQUIA(32)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 33); -- TURQUIA - ISRAEL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 34); -- TURQUIA - ARABIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 29); -- TURQUIA - RUSIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 28); -- TURQUIA - POLONIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 19); -- TURQUIA - EGIPTO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 35); -- TURQUIA - IRAN

--ISRAEL(33)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (33, 34); -- ISRAEL - ARABIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (33, 32); -- ISRAEL - TURQUIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (33, 19); -- ISRAEL - EGIPTO

--INDIA(36)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (36, 49); -- INDIA - SUMATRA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (36, 46); -- INDIA - MALASIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (36, 39); -- INDIA - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (36, 35); -- INDIA - IRAN

--MALASIA(46)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (46, 36); -- MALASIA - INDIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (46, 39); -- MALASIA - CHINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (46, 50); -- MALASIA - BORNEO

-- Oceanía
--SUMATRA(49)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (49, 48); -- SUMATRA - AUSTRALIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (49, 36); -- SUMATRA - INDIA
--BORNEO(50)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (50, 48); -- BORNEO - AUSTRALIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (50, 46); -- BORNEO - MALASIA
--JAVA(47)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (47, 48); -- JAVA - AUSTRALIA
--AUSTRALIA(48)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (48, 3);  -- AUSTRALIA - CHILE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (48, 47);  -- AUSTRALIA - JAVA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (48, 50);  -- AUSTRALIA - BORNEO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (48, 49);  -- AUSTRALIA - SUMATRA

-- Insertar objetivos secretos
INSERT INTO objectives (type, description, target_data, is_common) VALUES
                                                                       ('OCCUPATION', 'Ocupar África, 5 países de América del Norte y 4 países de Europa', '{"continents":["África"],"regions":[{"continent":"América del Norte","count":5},{"continent":"Europa","count":4}]}', false),
                                                                       ('OCCUPATION', 'Ocupar América del Sur, 7 países de Europa y 3 países limítrofes entre sí en cualquier lugar del mapa', '{"continents":["América del Sur"],"regions":[{"continent":"Europa","count":7}],"adjacent":3}', false),
                                                                       ('OCCUPATION', 'Ocupar Asia y 2 países de América del Sur', '{"continents":["Asia"],"regions":[{"continent":"América del Sur","count":2}]}', false),
                                                                       ('OCCUPATION', 'Ocupar Europa, 4 países de Asia y 2 países de América del Sur', '{"continents":["Europa"],"regions":[{"continent":"Asia","count":4},{"continent":"América del Sur","count":2}]}', false),
                                                                       ('OCCUPATION', 'Ocupar América del Norte, 2 países de Oceanía y 4 de Asia', '{"continents":["América del Norte"],"regions":[{"continent":"Oceanía","count":2},{"continent":"Asia","count":4}]}', false),
                                                                       ('OCCUPATION', 'Ocupar 2 países de Oceanía, 2 países de África, 2 países de América del Sur, 3 países de Europa, 4 de América del Norte y 3 de Asia', '{"regions":[{"continent":"Oceanía","count":2},{"continent":"África","count":2},{"continent":"América del Sur","count":2},{"continent":"Europa","count":3},{"continent":"América del Norte","count":4},{"continent":"Asia","count":3}]}', false),
                                                                       ('OCCUPATION', 'Ocupar Oceanía, América del Norte y 2 países de Europa', '{"continents":["Oceanía","América del Norte"],"regions":[{"continent":"Europa","count":2}]}', false),
                                                                       ('OCCUPATION', 'Ocupar América del Sur, África y 4 países de Asia', '{"continents":["América del Sur","África"],"regions":[{"continent":"Asia","count":4}]}', false),
                                                                       ('OCCUPATION', 'Ocupar Oceanía, África y 5 países de América del Norte', '{"continents":["Oceanía","África"],"regions":[{"continent":"América del Norte","count":5}]}', false),
                                                                       ('DESTRUCTION', 'Destruir el ejército azul, de ser imposible al jugador de la derecha', '{"targetColor":"BLUE"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército rojo, de ser imposible al jugador de la derecha', '{"targetColor":"RED"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército negro, de ser imposible al jugador de la derecha', '{"targetColor":"BLACK"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército amarillo, de ser imposible al jugador de la derecha', '{"targetColor":"YELLOW"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército verde, de ser imposible al jugador de la derecha', '{"targetColor":"GREEN"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército magenta, de ser imposible al jugador de la derecha', '{"targetColor":"PURPLE"}', false);

-- Insertar objetivo común
INSERT INTO objectives (type, description, target_data, is_common) VALUES
    ('COMMON', 'Ocupar 30 países', '{"territoryCount":30}', true);

-- Insertar cartas de países
INSERT INTO cards (country_id, type, is_in_deck)
SELECT id,
       CASE
           WHEN (id % 3) = 1 THEN 'INFANTRY'
           WHEN (id % 3) = 2 THEN 'CAVALRY'
           ELSE 'CANNON'
           END,
       true
FROM countries;

-- Insertar cartas wildcard
INSERT INTO cards (country_id, type, is_in_deck) VALUES
                                                     (NULL, 'WILDCARD', true),
                                                     (NULL, 'WILDCARD', true);

-- Insertar perfiles de bots
INSERT INTO bot_profiles (level, strategy, bot_name) VALUES
                                                         ('NOVICE', 'AGGRESSIVE', 'Bot Novato Agresivo'),
                                                         ('NOVICE', 'DEFENSIVE', 'Bot Novato Defensivo'),
                                                         ('NOVICE', 'BALANCED', 'Bot Novato Equilibrado'),
                                                         ('BALANCED', 'AGGRESSIVE', 'Bot Equilibrado Agresivo'),
                                                         ('BALANCED', 'DEFENSIVE', 'Bot Equilibrado Defensivo'),
                                                         ('BALANCED', 'BALANCED', 'Bot Equilibrado'),
                                                         ('BALANCED', 'OBJECTIVE_FOCUSED', 'Bot Enfocado en Objetivos'),
                                                         ('EXPERT', 'AGGRESSIVE', 'Bot Experto Agresivo'),
                                                         ('EXPERT', 'DEFENSIVE', 'Bot Experto Defensivo'),
                                                         ('EXPERT', 'BALANCED', 'Bot Experto Equilibrado'),
                                                         ('EXPERT', 'OBJECTIVE_FOCUSED', 'Bot Experto Estratega');

-- insertar usuarios para probar endopoints
INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (1, 'usuario1', 'usuario1@example.com', 'hash1', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (2, 'usuario2', 'usuario2@example.com', 'hash2', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (3, 'usuario3', 'usuario3@example.com', 'hash3', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (4, 'usuario4', 'usuario4@example.com', 'hash4', TRUE, CURRENT_TIMESTAMP);
INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (5, 'usuario5', 'usuario5@example.com', 'hash5', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (6, 'usuario6', 'usuario6@example.com', 'hash6', TRUE, CURRENT_TIMESTAMP);

