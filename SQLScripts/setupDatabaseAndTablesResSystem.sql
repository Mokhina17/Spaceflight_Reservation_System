-- ----- DO NOT CHANGE THE FOLLOWING SQL STATEMENTS -------
-- ------------- BUT EXECUTE THE STATEMENTS ---------------
-- create database
DROP DATABASE IF EXISTS reservation_system;
CREATE DATABASE reservation_system;

-- select database
USE reservation_system;

-- create customers table
CREATE TABLE `customers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstName` varchar(100) NOT NULL,
  `lastName` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `tokens` INT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `customers_UN` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- fill customers table with data
INSERT INTO reservation_system.customers (firstName,lastName,email,password) VALUES
('Maximilian','Maier','max.m@gmail.com','yxcvuioq')
                                                                                  ,('Karla','Arrowsmith','karla.a@gmx.de','kl,.p=q§we')
                                                                                  ,('Isabel','Raap','raap.i@web.de','vbnmopiu')
                                                                                  ,('Ted','Runkel','t.r@gmail.com','erzte89&')
                                                                                  ,('Josephine','Lukowski','josie.luko@gmx.de','qwerasdf')
                                                                                  ,('Hans','Massaro','hans.m@web.de','qwerqwer')
                                                                                  ,('Syble','Hocking','hocking.s@gmail.com','winter20')
                                                                                  ,('Crissy','Deaton','d.crissy@gmx.de','summer21')
                                                                                  ,('Edward','Vanmeter','van.ed@web.de','vfrzum21?')
                                                                                  ,('Jinny','Toews','toews.j@gmail.com','qwec16%9')
                                                                                  ,('Fiona','Metts','fiona.metts@gmail.com','1oij7&wer')
                                                                                  ,('Noah','Constantino','noah.const@web.de','9ikwelf%');

-- ------------------------ END ---------------------------

-- CREATE TABLES
-- dates (equivalent to dates)
CREATE TABLE `dates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- space companies (equivalent to cinemas)
CREATE TABLE `space_companies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `location` varchar(100) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- space flights (equivalent to movies)
CREATE TABLE `space_flights` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `flight_duration` int(11) NOT NULL,
  `view_type` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- flight schedules (equivalent to movie_playtimes)
CREATE TABLE `flight_schedules` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `companyId` int(11) NOT NULL,
  `flightId` int(11) NOT NULL,
  `launch_time` time NOT NULL,
  PRIMARY KEY (`id`),
  KEY `flight_schedules_FK` (`companyId`),
  KEY `flight_schedules_FK_1` (`flightId`),
  CONSTRAINT `flight_schedules_FK` FOREIGN KEY (`companyId`) REFERENCES `space_companies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `flight_schedules_FK_1` FOREIGN KEY (`flightId`) REFERENCES `space_flights` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- flight dates (equivalent to date_playtimes)
CREATE TABLE `flight_dates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `flightScheduleId` int(11) NOT NULL,
  `dateId` int(11) NOT NULL,
  `available_seats` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `flight_dates_FK_1` (`dateId`),
  KEY `flight_dates_FK` (`flightScheduleId`),
  CONSTRAINT `flight_dates_FK` FOREIGN KEY (`flightScheduleId`) REFERENCES `flight_schedules` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `flight_dates_FK_1` FOREIGN KEY (`dateId`) REFERENCES `dates` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- reservations
CREATE TABLE `reservations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customerId` int(11) NOT NULL,
  `flightDateId` int(11) NOT NULL,
  `reserved_seats` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `reservations_FK_1` (`customerId`),
  KEY `reservations_FK` (`flightDateId`),
  CONSTRAINT `reservations_FK` FOREIGN KEY (`flightDateId`) REFERENCES `flight_dates` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reservations_FK_1` FOREIGN KEY (`customerId`) REFERENCES `customers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- seat_numbers
CREATE TABLE `seat_numbers` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `reservationId` int(11) NOT NULL,
                                `seat_number` int(11) NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `seat_numbers_FK` (`reservationId`),
                                CONSTRAINT `seat_numbers_FK` FOREIGN KEY (`reservationId`) REFERENCES `reservations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- INSERT DATA
-- dates
INSERT INTO reservation_system.dates (`date`) VALUES
('2025-03-22'),('2025-04-17'),('2025-04-18'),('2025-04-19'),('2025-04-20'),('2025-04-21'),('2025-04-22'),('2025-04-23'),('2025-04-24'),('2025-04-25');

-- space companies
INSERT INTO reservation_system.space_companies (name,location,description) VALUES
('Virgin Galactic','Spaceport America, New Mexico','Virgin Galactic offers breathtaking space flight attractions that showcase the diverse beauty of continents like North America, South America, and Australia.')
                                                                                ,('SpaceX','Kennedy Space Center, Florida','SpaceX leads the commercial space industry with its innovative approach, providing unparalleled flight experiences to explore iconic views across continents such as Europe, Antarctica, and Asia.')
                                                                                ,('Blue Origin','Launch Site One, Texas','Blue Origin emphasizes safety and reliability while offering immersive space flight experiences highlighting the wonders of continents like Africa, South America, and Antarctica.')
                                                                                ,('Space Adventures','Baikonur Cosmodrome, Kazakhstan','Space Adventures provides exclusive flight experiences, enabling travelers to marvel at the landscapes of continents like Asia, Europe, and Australia from above.')
                                                                                ,('World View','Spaceport Tucson, Arizona','World View offers serene high-altitude balloon journeys, providing panoramic views of Earth’s continents, including North America, Europe, and Asia.');

-- space flights
INSERT INTO reservation_system.space_flights (name,flight_duration,view_type) VALUES
('Europe Journey',60,'Europe')
                                                                                   ,('Antarctica Experience',120,'Antarctica')
                                                                                   ,('Australia Flyby',90,'Australia')
                                                                                   ,('Africa Quest',100,'Africa')
                                                                                   ,('Asia Discovery',180,'Asia')
                                                                                   ,('South America Adventure',120,'South America')
                                                                                   ,('North America View',80,'North America');

-- flight schedules
INSERT INTO reservation_system.flight_schedules (companyId,flightId,launch_time) VALUES
(1,1,'13:30'),(1,3,'15:10'),(1,2,'13:00'),(1,2,'18:45'),(1,2,'15:00'),(1,2,'14:00')
                                                                                      ,(2,2,'11:45'),(2,2,'15:00'),(2,2,'12:30'),(2,2,'16:50'),(2,2,'17:30'),(2,2,'16:40')
                                                                                      ,(3,4,'19:30'),(3,5,'14:45')
                                                                                      ,(4,3,'15:15'),(4,6,'17:35'),(4,7,'20:00'),(4,1,'13:30'),(4,5,'21:15')
                                                                                      ,(5,2,'15:45'),(5,4,'18:30'),(5,5,'20:10'),(5,1,'14:30'),(5,3,'15:45'),(5,5,'15:00');

-- flight dates
INSERT INTO reservation_system.flight_dates (flightScheduleId,dateId,available_seats) VALUES
(1,1,50),(1,2,37),(1,3,53),(1,4,61),(1,5,13),(1,6,8),(1,7,31),(1,8,30),(1,9,18),(1,10,40)
                                                                                           ,(2,1,31),(2,5,84),(2,7,16),(3,8,14)
                                                                                           ,(4,1,30),(4,2,10),(4,3,30),(4,5,40),(4,6,61),(4,7,59)
                                                                                           ,(5,1,60),(5,4,30),(5,5,10),(5,6,19),(5,7,19)
                                                                                           ,(6,7,39),(20,4,45),(21,4,2),(22,7,1),(25,4,42),(22,5,42);

-- reservations
INSERT INTO reservation_system.reservations (customerId,flightDateId,reserved_seats) VALUES
(1,1,4),(1,1,10),(1,4,1),(1,1,100),(1,5,5),(1,1,10)
                                                                                          ,(2,2,2),(2,1,5),(3,5,6),(3,4,1),(4,8,3),(5,6,10),(5,2,2),(5,3,2),(5,11,4)
                                                                                          ,(6,5,3),(6,5,4),(6,31,5),(8,3,5),(8,6,2),(8,5,4),(8,18,1),(8,30,1),(8,13,1),(8,29,1),(8,19,1),(8,24,1),(8,31,1),(8,23,5)
                                                                                          ,(11,13,5),(11,2,1),(12,11,6),(12,31,10),(12,2,4);

-- Update existing customer data with tokens
UPDATE reservation_system.customers SET tokens = 0;

-- Insert sample data for reservationId = 1 and 2 for testing purposes
INSERT INTO seat_numbers (reservationId, seat_number)
VALUES
    (1, 5),
    (1, 6),
    (1, 7),
    (2, 10),
    (2, 11);
