# Database Schema Diagrams

```plantuml
@startuml
[[!include ./diagrams/database_diagram.puml]]
@enduml
```
# 📘 TEG Database Documentation

This document contains the complete SQL schema and insert statements for the **TEG** strategy game database. All elements are described in detail and structured for clarity.

## 🧱 Database Structure (Tables)

```sql
USE MASTER;
GO

CREATE DATABASE TEG;
GO

USE TEG;
GO

SET DATEFORMAT DMY;

--SET LANGUAGE Spanish;

CREATE TABLE States (
    id_state INT NOT NULL,
    description VARCHAR(50) NOT NULL,
    CONSTRAINT PK_States PRIMARY KEY (id_state)
);

CREATE TABLE Phases (
    id_phase INT NOT NULL,
    description VARCHAR(50) NOT NULL,
    CONSTRAINT PK_Phases PRIMARY KEY (id_phase)
);

CREATE TABLE Continents (
    id_continent INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_Continents PRIMARY KEY (id_continent)
);

CREATE TABLE Symbols (
    id_symbol INT NOT NULL,
    description VARCHAR(50) NOT NULL,
    CONSTRAINT PK_Symbols PRIMARY KEY (id_symbol)
);

CREATE TABLE TypeObjective (
    id_type INT NOT NULL,
    description VARCHAR(100) NOT NULL,
    CONSTRAINT PK_TypeObjective PRIMARY KEY (id_type)
);

CREATE TABLE Actions (
    id_action INT NOT NULL,
    description VARCHAR(100) NOT NULL,
    CONSTRAINT PK_Actions PRIMARY KEY (id_action)
);

CREATE TABLE Users (
    id_user INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    avatar VARCHAR(255),
    CONSTRAINT PK_Users PRIMARY KEY (id_user),
    CONSTRAINT UQ_Users_Username UNIQUE (username),
    CONSTRAINT UQ_Users_Email UNIQUE (email)
);

CREATE TABLE Bots (
    id_bot INT NOT NULL,
    difficulty INT NOT NULL,
    CONSTRAINT PK_Bots PRIMARY KEY (id_bot)
);

CREATE TABLE Games (
    id_game INT NOT NULL,
    id_state INT NOT NULL,
	id_phase INT NOT NULL,
    n_players INT NOT NULL,
    current_round INT,
    time_limit INT,
    rules VARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT PK_Games PRIMARY KEY (id_game),
    CONSTRAINT FK_Games_States FOREIGN KEY (id_state)
        REFERENCES States (id_state),
	CONSTRAINT FK_Games_Phases FOREIGN KEY (id_phase)
	REFERENCES Phases (id_phase)
);

CREATE TABLE Objectives (
    id_objective INT NOT NULL,
    description VARCHAR(MAX) NOT NULL,
    id_type INT NOT NULL,
    CONSTRAINT PK_Objectives PRIMARY KEY (id_objective),
    CONSTRAINT FK_Objectives_TypeObjective FOREIGN KEY (id_type)
        REFERENCES TypeObjective (id_type)
);

CREATE TABLE Players (
    id_player INT NOT NULL,
    id_game INT NOT NULL,
    id_user INT NULL,
    id_bot INT NULL,
    id_objective INT NOT NULL,
    seat_order INT NOT NULL,
    status VARCHAR(20),
    CONSTRAINT PK_Players PRIMARY KEY (id_player),
    CONSTRAINT FK_Players_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game),
    CONSTRAINT FK_Players_User FOREIGN KEY (id_user)
        REFERENCES Users (id_user),
    CONSTRAINT FK_Players_Bot FOREIGN KEY (id_bot)
        REFERENCES Bots (id_bot),
    CONSTRAINT FK_Players_Objective FOREIGN KEY (id_objective)
        REFERENCES Objectives (id_objective),
    CONSTRAINT UQ_Players_GameSeat UNIQUE (id_game, seat_order),
    CONSTRAINT CHK_Players_UserOrBot CHECK (
        (id_user IS NOT NULL AND id_bot IS NULL) OR (id_user IS NULL AND id_bot IS NOT NULL)
    )
);

CREATE TABLE Countries (
    id_country INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    id_continent INT NOT NULL,
    CONSTRAINT PK_Countries PRIMARY KEY (id_country),
    CONSTRAINT FK_Countries_Continent FOREIGN KEY (id_continent)
        REFERENCES Continents (id_continent)
);

CREATE TABLE Borders (
    id_country1 INT NOT NULL,
    id_country2 INT NOT NULL,
    CONSTRAINT PK_Borders PRIMARY KEY (id_country1, id_country2),
    CONSTRAINT FK_Borders_Country1 FOREIGN KEY (id_country1)
        REFERENCES Countries (id_country),
    CONSTRAINT FK_Borders_Country2 FOREIGN KEY (id_country2)
        REFERENCES Countries (id_country)
);

CREATE TABLE GameTerritories (
    id_game INT NOT NULL,
    id_country INT NOT NULL,
    id_player INT,
    n_armies INT DEFAULT 0,
    CONSTRAINT PK_GameTerritories PRIMARY KEY (id_game, id_country),
    CONSTRAINT FK_GameTerritories_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game),
    CONSTRAINT FK_GameTerritories_Country FOREIGN KEY (id_country)
        REFERENCES Countries (id_country),
    CONSTRAINT FK_GameTerritories_Player FOREIGN KEY (id_player)
        REFERENCES Players (id_player)
);

CREATE TABLE Cards (
    id_card INT NOT NULL,
    id_country INT NOT NULL,
    id_symbol INT NOT NULL,
    CONSTRAINT PK_Cards PRIMARY KEY (id_card),
    CONSTRAINT FK_Cards_Country FOREIGN KEY (id_country)
        REFERENCES Countries (id_country),
    CONSTRAINT FK_Cards_Symbol FOREIGN KEY (id_symbol)
        REFERENCES Symbols (id_symbol)
);

CREATE TABLE Player_Hand (
    id_card_player INT NOT NULL,
    id_card INT NOT NULL,
    id_player INT NOT NULL,
    id_game INT NOT NULL,
    is_used BIT DEFAULT 0,
    CONSTRAINT PK_PlayerHand PRIMARY KEY (id_card_player),
    CONSTRAINT FK_PlayerHand_Card FOREIGN KEY (id_card)
        REFERENCES Cards (id_card),
    CONSTRAINT FK_PlayerHand_Player FOREIGN KEY (id_player)
        REFERENCES Players (id_player),
    CONSTRAINT FK_PlayerHand_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game)
);

CREATE TABLE TurnTimer (
    id_timer INT NOT NULL,
    id_game INT NOT NULL,
    id_player INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    is_timeout BIT,
    CONSTRAINT PK_TurnTimer PRIMARY KEY (id_timer),
    CONSTRAINT FK_TurnTimer_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game),
    CONSTRAINT FK_TurnTimer_Player FOREIGN KEY (id_player)
        REFERENCES Players (id_player)
);

CREATE TABLE GameSnapshots (
    id_snapshot INT NOT NULL,
    id_game INT NOT NULL,
    turn_number INT NOT NULL,
    data_state VARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT PK_GameSnapshots PRIMARY KEY (id_snapshot),
    CONSTRAINT FK_GameSnapshots_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game)
);

CREATE TABLE Messages (
    id_message INT NOT NULL,
    id_game INT NOT NULL,
    id_player INT NOT NULL,
    time_sent DATETIME,
    message VARCHAR(MAX) NOT NULL,
    CONSTRAINT PK_Messages PRIMARY KEY (id_message),
    CONSTRAINT FK_Messages_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game),
    CONSTRAINT FK_Messages_Player FOREIGN KEY (id_player)
        REFERENCES Players (id_player)
);

CREATE TABLE GameEvents (
    id_event INT NOT NULL,
    id_game INT NOT NULL,
    id_player INT NOT NULL,
    id_action INT NOT NULL,
    event_info VARCHAR(MAX),
    datetime DATETIME,
    CONSTRAINT PK_GameEvents PRIMARY KEY (id_event),
    CONSTRAINT FK_GameEvents_Game FOREIGN KEY (id_game)
        REFERENCES Games (id_game),
    CONSTRAINT FK_GameEvents_Player FOREIGN KEY (id_player)
        REFERENCES Players (id_player),
    CONSTRAINT FK_GameEvents_Action FOREIGN KEY (id_action)
        REFERENCES Actions (id_action)
);
```
## 📥 Initial Data (INSERT Statements)

```sql
USE TEG

-----------------------INSERTS TABLA STATES (estados)------------------------------
INSERT INTO States (id_state, description) VALUES 
(1, 'Esperando jugadores'),
(2, 'En curso'),
(3, 'Finalizado'),
(4, 'Cancelado'),
(5, 'Pausa');


----------------------------INSERTS TABLA PHASES(fases)---------------------------------
INSERT INTO Phases (id_phase, description) VALUES
(1, 'Inicio de turno'),
(2, 'Colocación de ejércitos'),
(3, 'Ataque'),
(4, 'Reagrupamiento'),
(5, 'Fin de turno');


----------------------------INSERTS TABLA Continents(continentes)----------------------------
INSERT INTO Continents (id_continent, name) VALUES
(1, 'América del Norte'),
(2, 'América del Sur'),
(3, 'Europa'),
(4, 'África'),
(5, 'Asia'),
(6, 'Oceanía');

--------------------------INSERTS Symbols(símbolos)---------Los 3 comunes del teg--------------
INSERT INTO Symbols (id_symbol, description) VALUES
(1, 'Globo'),
(2, 'Cañón'),
(3, 'Barco');

------------------------INSERTS de TypeObjetives(tipos de objetivos)---------------------
INSERT INTO TypeObjective (id_type, description) VALUES
(1, 'Conquista (Continentes y Países)'),  -- Conquistar territorio ya sean paises o continentes
(2, 'Eliminación')  -- para eliminar a un jugador


-------------------------INSERTS TABLA Actions (acciones)------------------------------
INSERT INTO Actions (id_action, description) VALUES
(1, 'Atacar'),
(2, 'Defender'),
(3, 'Mover o reagrupar tropas'),
(4, 'Conquistar territorio'),
(5, 'Cambiar cartas'),
(6, 'Reforzar tropas');

--------------------------------INSERTS TABLA Bots---------------------------------------
INSERT INTO Bots (id_bot, difficulty) VALUES
(1, 1),  -- Nivel fácil
(2, 2),  -- Nivel medio
(3, 3);  -- Nivel difícil

--------------------------------INSERTS TABLA Objetives------------------------------------
INSERT INTO Objectives (id_objective, description, id_type) VALUES   --tipo conquista
(1, 'Ocupar Europa y América del Sur', 1),
(2, 'Ocupar América del Norte, Oceanía y 5 países de África', 1),
(3, 'Ocupar Asia y América Central', 1),
(4, 'Ocupar América del Norte, 8 países de Asia y 4 de Europa', 1),
(5, 'Ocupar 4 países de América del Norte, 4 de Europa, 4 de Asia, 3 de América del Sur, 3 de África y 3 de Oceanía', 1),
(6, 'Ocupar Oceanía, 6 países de Asia, 6 de África y 6 de América del Norte', 1),
(7, 'Ocupar Africa, 6 países de América del Sur, 6 de Europa y 6 de Asia', 1),
(8, 'Ocupar América del Sur, África y 8 países de Asia', 1),
(9, 'Ocupar Oceanía, África, 4 países de América del Norte y 4 de Asia', 1),
(10, 'Ocupar Europa, 4 países de Asia y 4 países de América del Sur', 1),
(11, 'Ocupar 35 países en cualquier lugar del mapa', 1);

-- Insertar objetivos de tipo Eliminación (Tipo 2)
INSERT INTO Objectives (id_objective, description, id_type) VALUES	--tipo eliminacion
(12, 'Destruir al ejército Blanco; de no ser posible, al jugador de la derecha', 2),
(13, 'Destruir al ejército Negro; de no ser posible, al jugador de la derecha', 2),
(14, 'Destruir al ejército Rojo; de no ser posible, al jugador de la derecha', 2),
(15, 'Destruir al ejército Azul; de no ser posible, al jugador de la derecha', 2),
(16, 'Destruir al ejército Amarillo; de no ser posible, al jugador de la derecha', 2),
(17, 'Destruir al ejército Verde; de no ser posible, al jugador de la derecha', 2),
(18, 'Destruir al jugador de la izquierda', 2),
(19, 'Objetivo común ocupar Todos los países', 2)

---------------------------------INSERTS TABLA Countries (paises)--------------------

INSERT INTO Countries (id_country, name, id_continent) VALUES
--america del norte
(1, 'Alaska', 1),
(2, 'Yukon', 1),
(3, 'Oregon', 1),
(4, 'California', 1),
(5, 'Mexico', 1),
(6, 'Nueva York', 1),
(7, 'Terranova', 1),
(8, 'Labrador', 1),
(9, 'Groenlandia', 1),
(10, 'Canada', 1),

--america del sur
(11, 'Colombia', 2),
(12, 'Peru', 2),
(13, 'Chile', 2),
(14, 'Argentina', 2),
(15, 'Uruguay', 2),
(16, 'Brasil', 2),

--europa
(17, 'Islandia', 3),
(18, 'Gran Bretaña', 3),
(19, 'Suecia', 3),
(20, 'Rusia', 3),
(21, 'Polonia', 3),
(22, 'Alemania', 3),
(23, 'Francia', 3),
(24, 'Italia', 3),
(25, 'España', 3),

--africa
(26, 'Egipto', 4),
(27, 'Sahara', 4),
(28, 'Etiopía', 4),
(29, 'Zaire', 4),
(30, 'Sud Africa', 4),
(31, 'Madagascar', 4),

--asia
(32, 'Turquía', 5),
(33, 'Israel', 5),
(34, 'Arabia', 5),
(35, 'Irán', 5),
(36, 'India', 5),
(37, 'Siberia', 5),
(38, 'Tartaria', 5),
(39, 'Tamir', 5),
(40, 'Aral', 5),
(41, 'Mongolia', 5),
(42, 'Gobi', 5),
(43, 'China', 5),
(44, 'Kamchatka', 5),
(45, 'Japón', 5),
(46, 'Malasia', 5),

--oceanía
(47, 'Sumatra', 6),
(48, 'Java', 6),
(49, 'Borneo', 6),
(50, 'Australia', 6)



--------------------------INSERTS TABLA BORDERS----------------------------

--AMERICA DEL NORTE CONEXIONES entre paises
INSERT INTO Borders (id_country1, id_country2) VALUES (9, 8); -- Groenlandia -> Labrador
INSERT INTO Borders (id_country1, id_country2) VALUES (9, 6); -- Groenlandia -> Nueva York
INSERT INTO Borders (id_country1, id_country2) VALUES (8, 9); -- Labrador -> Groenlandia
INSERT INTO Borders (id_country1, id_country2) VALUES (8, 7); -- Labrador -> Terranova
INSERT INTO Borders (id_country1, id_country2) VALUES (7, 8); -- Terranova -> Labrador
INSERT INTO Borders (id_country1, id_country2) VALUES (7, 10); -- Terranova -> Canadá
INSERT INTO Borders (id_country1, id_country2) VALUES (7, 6); -- Terranova -> Nueva York
INSERT INTO Borders (id_country1, id_country2) VALUES (6, 9);  -- Nueva York -> Groenlandia
INSERT INTO Borders (id_country1, id_country2) VALUES (6, 7);  -- Nueva York -> Terranova
INSERT INTO Borders (id_country1, id_country2) VALUES (6, 10); -- Nueva York -> Canadá
INSERT INTO Borders (id_country1, id_country2) VALUES (6, 3);  -- Nueva York -> Oregon
INSERT INTO Borders (id_country1, id_country2) VALUES (6, 4);  -- Nueva York -> California
INSERT INTO Borders (id_country1, id_country2) VALUES (10, 7); -- Canadá -> Terranova
INSERT INTO Borders (id_country1, id_country2) VALUES (10, 6); -- Canadá -> Nueva York
INSERT INTO Borders (id_country1, id_country2) VALUES (10, 2); -- Canadá -> Yukon
INSERT INTO Borders (id_country1, id_country2) VALUES (10, 3); -- Canadá -> Oregon
INSERT INTO Borders (id_country1, id_country2) VALUES (2, 10); -- Yukon -> Canadá
INSERT INTO Borders (id_country1, id_country2) VALUES (2, 1);  -- Yukon -> Alaska
INSERT INTO Borders (id_country1, id_country2) VALUES (2, 3);  -- Yukon -> Oregon
INSERT INTO Borders (id_country1, id_country2) VALUES (1, 2); -- Alaska -> Yukon
INSERT INTO Borders (id_country1, id_country2) VALUES (1, 3); -- Alaska -> Oregon
INSERT INTO Borders (id_country1, id_country2) VALUES (3, 1);  -- Oregon -> Alaska
INSERT INTO Borders (id_country1, id_country2) VALUES (3, 2);  -- Oregon -> Yukon
INSERT INTO Borders (id_country1, id_country2) VALUES (3, 10); -- Oregon -> Canadá
INSERT INTO Borders (id_country1, id_country2) VALUES (3, 6);  -- Oregon -> Nueva York
INSERT INTO Borders (id_country1, id_country2) VALUES (3, 4);  -- Oregon -> California
INSERT INTO Borders (id_country1, id_country2) VALUES (4, 3);  -- California -> Oregon
INSERT INTO Borders (id_country1, id_country2) VALUES (4, 6);  -- California -> Nueva York
INSERT INTO Borders (id_country1, id_country2) VALUES (4, 5);  -- California -> México
INSERT INTO Borders (id_country1, id_country2) VALUES (5, 4); -- México -> California


----AMERICA DEL SUR--
INSERT INTO Borders (id_country1, id_country2) VALUES (11, 12); -- Colombia -> Perú
INSERT INTO Borders (id_country1, id_country2) VALUES (11, 16); -- Colombia -> Brasil
INSERT INTO Borders (id_country1, id_country2) VALUES (12, 11); -- Perú -> Colombia
INSERT INTO Borders (id_country1, id_country2) VALUES (12, 13); -- Perú -> Chile
INSERT INTO Borders (id_country1, id_country2) VALUES (12, 16); -- Perú -> Brasil
INSERT INTO Borders (id_country1, id_country2) VALUES (12, 14); -- Perú -> Argentina
INSERT INTO Borders (id_country1, id_country2) VALUES (16, 15); -- Brasil -> Uruguay
INSERT INTO Borders (id_country1, id_country2) VALUES (16, 11); -- Brasil -> Colombia
INSERT INTO Borders (id_country1, id_country2) VALUES (16, 12); -- Brasil -> Perú
INSERT INTO Borders (id_country1, id_country2) VALUES (16, 14); -- Brasil -> Argentina
INSERT INTO Borders (id_country1, id_country2) VALUES (15, 14); -- Uruguay -> Argentina
INSERT INTO Borders (id_country1, id_country2) VALUES (15, 16); -- Uruguay -> Brasil
INSERT INTO Borders (id_country1, id_country2) VALUES (14, 13); -- Argentina -> Chile
INSERT INTO Borders (id_country1, id_country2) VALUES (14, 12); -- Argentina -> Perú
INSERT INTO Borders (id_country1, id_country2) VALUES (14, 16); -- Argentina -> Brasil
INSERT INTO Borders (id_country1, id_country2) VALUES (14, 15); -- Argentina -> Uruguay
INSERT INTO Borders (id_country1, id_country2) VALUES (13, 12); -- Chile -> Perú
INSERT INTO Borders (id_country1, id_country2) VALUES (13, 14); -- Chile -> Argentina


---EUROPA----------------------------------
INSERT INTO Borders (id_country1, id_country2) VALUES (17, 19); -- Islandia -> Suecia
INSERT INTO Borders (id_country1, id_country2) VALUES (17, 18); -- Islandia -> Gran Bretaña
INSERT INTO Borders (id_country1, id_country2) VALUES (18, 17); -- Gran Bretaña -> Islandia
INSERT INTO Borders (id_country1, id_country2) VALUES (18, 22); -- Gran Bretaña -> Alemania
INSERT INTO Borders (id_country1, id_country2) VALUES (18, 25); -- Gran Bretaña -> España
INSERT INTO Borders (id_country1, id_country2) VALUES (19, 17); -- Suecia -> Islandia
INSERT INTO Borders (id_country1, id_country2) VALUES (19, 20); -- Suecia -> Rusia
INSERT INTO Borders (id_country1, id_country2) VALUES (20, 22); -- Rusia -> Polonia
INSERT INTO Borders (id_country1, id_country2) VALUES (20, 19); -- Rusia -> Suecia
INSERT INTO Borders (id_country1, id_country2) VALUES (21, 20); -- Polonia -> Rusia
INSERT INTO Borders (id_country1, id_country2) VALUES (21, 22); -- Polonia -> Alemania
INSERT INTO Borders (id_country1, id_country2) VALUES (22, 21); -- Alemania -> Polonia
INSERT INTO Borders (id_country1, id_country2) VALUES (22, 18); -- Alemania -> Gran Bretaña
INSERT INTO Borders (id_country1, id_country2) VALUES (22, 24); -- Alemania -> Italia
INSERT INTO Borders (id_country1, id_country2) VALUES (22, 23); -- Alemania -> Francia
INSERT INTO Borders (id_country1, id_country2) VALUES (23, 24); -- Francia -> Italia
INSERT INTO Borders (id_country1, id_country2) VALUES (23, 22); -- Francia -> Alemania
INSERT INTO Borders (id_country1, id_country2) VALUES (23, 25); -- Francia -> España
INSERT INTO Borders (id_country1, id_country2) VALUES (25, 23); -- España -> Francia
INSERT INTO Borders (id_country1, id_country2) VALUES (25, 18); -- España -> Gran Bretaña
INSERT INTO Borders (id_country1, id_country2) VALUES (24, 22); -- Italia -> Alemania
INSERT INTO Borders (id_country1, id_country2) VALUES (24, 23); -- Italia -> Francia


--AFRICA-----------------------------------
INSERT INTO Borders (id_country1, id_country2) VALUES (31, 26); -- Madagascar -> Egipto
INSERT INTO Borders (id_country1, id_country2) VALUES (31, 29); -- Madagascar -> Zaire
INSERT INTO Borders (id_country1, id_country2) VALUES (26, 31); -- Egipto -> Madagascar
INSERT INTO Borders (id_country1, id_country2) VALUES (26, 28); -- Egipto -> Etiopía
INSERT INTO Borders (id_country1, id_country2) VALUES (26, 27); -- Egipto -> Sahara
INSERT INTO Borders (id_country1, id_country2) VALUES (27, 26); -- Sahara -> Egipto
INSERT INTO Borders (id_country1, id_country2) VALUES (27, 28); -- Sahara -> Etiopía
INSERT INTO Borders (id_country1, id_country2) VALUES (27, 29); -- Sahara -> Zaire
INSERT INTO Borders (id_country1, id_country2) VALUES (28, 26); -- Etiopía -> Egipto
INSERT INTO Borders (id_country1, id_country2) VALUES (28, 27); -- Etiopía -> Sahara
INSERT INTO Borders (id_country1, id_country2) VALUES (28, 29); -- Etiopía -> Zaire
INSERT INTO Borders (id_country1, id_country2) VALUES (28, 30); -- Etiopía -> Sudáfrica
INSERT INTO Borders (id_country1, id_country2) VALUES (29, 27); -- Zaire -> Sahara
INSERT INTO Borders (id_country1, id_country2) VALUES (29, 28); -- Zaire -> Etiopía
INSERT INTO Borders (id_country1, id_country2) VALUES (29, 30); -- Zaire -> Sudáfrica
INSERT INTO Borders (id_country1, id_country2) VALUES (29, 31); -- Zaire -> Madagascar
INSERT INTO Borders (id_country1, id_country2) VALUES (30, 29); -- Sudáfrica -> Zaire
INSERT INTO Borders (id_country1, id_country2) VALUES (30, 28); -- Sudáfrica -> Etiopía


---ASIA---------------------------
INSERT INTO Borders (id_country1, id_country2) VALUES (40, 38); -- Aral -> Tartaria
INSERT INTO Borders (id_country1, id_country2) VALUES (40, 37); -- Aral -> Siberia
INSERT INTO Borders (id_country1, id_country2) VALUES (40, 35); -- Aral -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (38, 40); -- Tartaria -> Aral
INSERT INTO Borders (id_country1, id_country2) VALUES (38, 39); -- Tartaria -> Tamir
INSERT INTO Borders (id_country1, id_country2) VALUES (38, 37); -- Tartaria -> Siberia
INSERT INTO Borders (id_country1, id_country2) VALUES (39, 38); -- Tamir -> Tartaria
INSERT INTO Borders (id_country1, id_country2) VALUES (39, 37); -- Tamir -> Siberia
INSERT INTO Borders (id_country1, id_country2) VALUES (37, 44); -- Siberia -> Kamchatka
INSERT INTO Borders (id_country1, id_country2) VALUES (37, 43); -- Siberia -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (37, 41); -- Siberia -> Mongolia
INSERT INTO Borders (id_country1, id_country2) VALUES (37, 39); -- Siberia -> Tamir
INSERT INTO Borders (id_country1, id_country2) VALUES (37, 38); -- Siberia -> Tartaria
INSERT INTO Borders (id_country1, id_country2) VALUES (37, 40); -- Siberia -> Aral
INSERT INTO Borders (id_country1, id_country2) VALUES (41, 40); -- Mongolia -> Aral
INSERT INTO Borders (id_country1, id_country2) VALUES (41, 37); -- Mongolia -> Siberia
INSERT INTO Borders (id_country1, id_country2) VALUES (41, 35); -- Mongolia -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (41, 42); -- Mongolia -> Gobi
INSERT INTO Borders (id_country1, id_country2) VALUES (41, 43); -- Mongolia -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (44, 37); -- Kamchatka -> Siberia
INSERT INTO Borders (id_country1, id_country2) VALUES (44, 45); -- Kamchatka -> Japón
INSERT INTO Borders (id_country1, id_country2) VALUES (44, 43); -- Kamchatka -> China

INSERT INTO Borders (id_country1, id_country2) VALUES (45, 44); -- Japón -> Kamchatka
INSERT INTO Borders (id_country1, id_country2) VALUES (45, 43); -- Japón -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 45); -- China -> Japón
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 44); -- China -> Kamchatka
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 37); -- China -> Siberia
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 41); -- China -> Mongolia
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 42); -- China -> Gobi
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 35); -- China -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 36); -- China -> India
INSERT INTO Borders (id_country1, id_country2) VALUES (43, 46); -- China -> Malasia
INSERT INTO Borders (id_country1, id_country2) VALUES (42, 43); -- Gobi -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (42, 41); -- Gobi -> Mongolia
INSERT INTO Borders (id_country1, id_country2) VALUES (42, 35); -- Gobi -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 40); -- Irán -> Aral
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 41); -- Irán -> Mongolia
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 42); -- Irán -> Gobi
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 43); -- Irán -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 36); -- Irán -> India
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 32); -- Irán -> Turquía
INSERT INTO Borders (id_country1, id_country2) VALUES (32, 35); -- Turquía -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (32, 33); -- Turquía -> Israel
INSERT INTO Borders (id_country1, id_country2) VALUES (32, 34); -- Turquía -> Arabia
INSERT INTO Borders (id_country1, id_country2) VALUES (33, 32); -- Israel -> Turquía
INSERT INTO Borders (id_country1, id_country2) VALUES (33, 34); -- Israel -> Arabia
INSERT INTO Borders (id_country1, id_country2) VALUES (34, 32); -- Arabia -> Turquía
INSERT INTO Borders (id_country1, id_country2) VALUES (34, 33); -- Arabia -> Israel
INSERT INTO Borders (id_country1, id_country2) VALUES (36, 43); -- India -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (36, 46); -- India -> Malasia
INSERT INTO Borders (id_country1, id_country2) VALUES (36, 35); -- India -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (46, 43); -- Malasia -> China
INSERT INTO Borders (id_country1, id_country2) VALUES (46, 36); -- Malasia -> India



---OCEANIA---
INSERT INTO Borders (id_country1, id_country2) VALUES (50, 47); -- Australia -> Sumatra
INSERT INTO Borders (id_country1, id_country2) VALUES (50, 49); -- Australia -> Borneo
INSERT INTO Borders (id_country1, id_country2) VALUES (50, 48); -- Australia -> Java

INSERT INTO Borders (id_country1, id_country2) VALUES (47, 50); -- Sumatra -> Australia
INSERT INTO Borders (id_country1, id_country2) VALUES (49, 50); -- Borneo -> Australia
INSERT INTO Borders (id_country1, id_country2) VALUES (48, 50); -- Java -> Australia



----------------UNIONES ENTRE CONTINENTES y Uniones Por Océano----------------------
INSERT INTO Borders (id_country1, id_country2) VALUES (44, 1);  -- Kamchatka -> Alaska
INSERT INTO Borders (id_country1, id_country2) VALUES (1, 44);  -- Alaska -> Kamchatka		
INSERT INTO Borders (id_country1, id_country2) VALUES (9, 17);  -- Groenlandia -> Islandia
INSERT INTO Borders (id_country1, id_country2) VALUES (17, 9);  -- Islandia -> Groenlandia
INSERT INTO Borders (id_country1, id_country2) VALUES (5, 11);  -- México -> Colombia
INSERT INTO Borders (id_country1, id_country2) VALUES (11, 5);  -- Colombia -> México
INSERT INTO Borders (id_country1, id_country2) VALUES (16, 27);  -- Brasil -> Sahara
INSERT INTO Borders (id_country1, id_country2) VALUES (27, 16);  -- Sahara -> Brasil
INSERT INTO Borders (id_country1, id_country2) VALUES (27, 25);  -- Sahara -> España
INSERT INTO Borders (id_country1, id_country2) VALUES (25, 27);  -- España -> Sahara

--africa con asia
INSERT INTO Borders (id_country1, id_country2) VALUES (26, 21);  -- Egipto -> Polonia
INSERT INTO Borders (id_country1, id_country2) VALUES (21, 26);  -- Polonia -> Egipto


INSERT INTO Borders (id_country1, id_country2) VALUES (26, 32);  -- Egipto -> Turquía
INSERT INTO Borders (id_country1, id_country2) VALUES (32, 26);  -- Turquía -> Egipto

-- De Egipto a Israel y viceversa
INSERT INTO Borders (id_country1, id_country2) VALUES (26, 33);  -- Egipto -> Israel
INSERT INTO Borders (id_country1, id_country2) VALUES (33, 26);  -- Israel -> Egipto

--america con oceania
INSERT INTO Borders (id_country1, id_country2) VALUES (50, 13);  -- Australia -> Chile
INSERT INTO Borders (id_country1, id_country2) VALUES (13, 50);  -- Chile -> Australia

--oceania con asia
INSERT INTO Borders (id_country1, id_country2) VALUES (47, 36);  -- Sumatra -> India
INSERT INTO Borders (id_country1, id_country2) VALUES (36, 47);  -- India -> Sumatra
INSERT INTO Borders (id_country1, id_country2) VALUES (49, 46);  -- Borneo -> Malasia
INSERT INTO Borders (id_country1, id_country2) VALUES (46, 49);  -- Malasia -> Borneo

--asia con europa
INSERT INTO Borders (id_country1, id_country2) VALUES (32, 21);  -- Turquía -> Polonia
INSERT INTO Borders (id_country1, id_country2) VALUES (21, 32);  -- Polonia -> Turquía
INSERT INTO Borders (id_country1, id_country2) VALUES (32, 20);  -- Turquía -> Rusia
INSERT INTO Borders (id_country1, id_country2) VALUES (20, 32);  -- Rusia -> Turquía
INSERT INTO Borders (id_country1, id_country2) VALUES (35, 20);  -- Irán -> Rusia
INSERT INTO Borders (id_country1, id_country2) VALUES (20, 35);  -- Rusia -> Irán
INSERT INTO Borders (id_country1, id_country2) VALUES (40, 20);  -- Aral -> Rusia
INSERT INTO Borders (id_country1, id_country2) VALUES (20, 40);  -- Rusia -> Aral





-------------------INSERTS DE TABLA Cards (cartas)-----------------------------------
-- Globo (id_symbol = 1)
INSERT INTO Cards (id_card, id_country, id_symbol) VALUES
(1, 1, 1),  -- Alaska
(2, 2, 1),  -- Yukon
(3, 3, 1),  -- Oregon
(4, 4, 1),  -- California
(5, 5, 1),  -- Mexico
(6, 6, 1),  -- Nueva York
(7, 7, 1),  -- Terranova
(8, 8, 1),  -- Labrador
(9, 9, 1),  -- Groenlandia
(10, 10, 1), -- Canada
(11, 11, 1), -- Colombia
(12, 12, 1), -- Peru
(13, 13, 1), -- Chile
(14, 14, 1), -- Argentina
(15, 15, 1), -- Uruguay
(16, 16, 1), -- Brasil
(17, 17, 1); -- Islandia

-- Cañón (id_symbol = 2)
INSERT INTO Cards (id_card, id_country, id_symbol) VALUES
(18, 18, 2),  -- Gran Bretaña
(19, 19, 2),  -- Suecia
(20, 20, 2),  -- Rusia
(21, 21, 2),  -- Polonia
(22, 22, 2),  -- Alemania
(23, 23, 2),  -- Francia
(24, 24, 2),  -- Italia
(25, 25, 2),  -- España
(26, 26, 2),  -- Egipto
(27, 27, 2),  -- Sahara
(28, 28, 2),  -- Etiopía
(29, 29, 2),  -- Zaire
(30, 30, 2),  -- Sud África
(31, 31, 2),  -- Madagascar
(32, 32, 2),  -- Turquía
(33, 33, 2),  -- Israel
(34, 34, 2);  -- Arabia

-- Barco (id_symbol = 3)
INSERT INTO Cards (id_card, id_country, id_symbol) VALUES
(35, 35, 3),  -- Irán
(36, 36, 3),  -- India
(37, 37, 3),  -- Siberia
(38, 38, 3),  -- Tartaria
(39, 39, 3),  -- Tamir
(40, 40, 3),  -- Aral
(41, 41, 3),  -- Mongolia
(42, 42, 3),  -- Gobi
(43, 43, 3),  -- China
(44, 44, 3),  -- Kamchatka
(45, 45, 3),  -- Japón
(46, 46, 3),  -- Malasia
(47, 47, 3),  -- Sumatra
(48, 48, 3),  -- Java
(49, 49, 3),  -- Borneo
(50, 50, 3);  -- Australia

```

