-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: isc_ms
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '4e9d1aa2-a46b-11f0-92a1-e3a0787afb7b:1-283';

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event` (
  `event_id` int NOT NULL AUTO_INCREMENT,
  `event_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` enum('FITNESS','YOGA','SWIMMING','HIIT','WORKSHOP','VIP_ONLY','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `capacity` smallint NOT NULL,
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `min_tier` enum('CLASSIC','GOLD','VIP') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CLASSIC',
  `early_access_hours` smallint NOT NULL DEFAULT '24',
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` enum('ACTIVE','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_by` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `fk_event_manager` (`created_by`),
  KEY `idx_event_date` (`event_date`),
  KEY `idx_event_status` (`status`),
  CONSTRAINT `fk_event_manager` FOREIGN KEY (`created_by`) REFERENCES `manager` (`manager_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'Belly Burn','HIIT','2026-03-25','17:30:00','18:15:00','İstanbul Sports Club',15,0.00,'GOLD',24,'An intermediate-level workout to activate the abdominal (core) area.','ACTIVE',1,'2026-03-21 17:38:57');
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_registration`
--

DROP TABLE IF EXISTS `event_registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_registration` (
  `registration_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `event_id` int NOT NULL,
  `registration_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_status` enum('FREE','PAID','PENDING','WAITLISTED','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FREE',
  `waitlist_position` smallint DEFAULT NULL,
  PRIMARY KEY (`registration_id`),
  UNIQUE KEY `uq_member_event` (`member_id`,`event_id`),
  KEY `fk_reg_event` (`event_id`),
  CONSTRAINT `fk_reg_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `fk_reg_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_registration`
--

LOCK TABLES `event_registration` WRITE;
/*!40000 ALTER TABLE `event_registration` DISABLE KEYS */;
INSERT INTO `event_registration` VALUES (3,1,1,'2026-03-23 09:53:48','FREE',NULL);
/*!40000 ALTER TABLE `event_registration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `manager`
--

DROP TABLE IF EXISTS `manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manager` (
  `manager_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','MANAGER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MANAGER',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `failed_attempts` tinyint NOT NULL DEFAULT '0',
  `is_locked` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`manager_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manager`
--

LOCK TABLES `manager` WRITE;
/*!40000 ALTER TABLE `manager` DISABLE KEYS */;
INSERT INTO `manager` VALUES (1,'Admin Manager','admin','admin@isc.com','ADMIN','$2a$12$KZC7bQDsN/A7AJtwdoRgAO0lhRhljZcTWOtuH7.zTilR9E7kwlIje',0,0,'2026-03-21 13:33:34'),(2,'Cahit Yunus Özdikiş','yunusozdıkıs','yunusozdıkıs@isc.com','MANAGER','$2a$12$J5bDVkBmNZzw47sGKOXqiuYoM0bqfqcOH2CZNFscRMniIn2Hnf1pO',0,0,'2026-03-21 18:33:35'),(3,'Tuna Küni','tunakuni','tunakuni@isc.com','MANAGER','$2a$12$bN/q/t2Gh6rGWrwTWGfjmuuoJw2TNvhyE8xVsiv7Yl2TK/CHxI3hq',0,0,'2026-03-21 18:34:07');
/*!40000 ALTER TABLE `manager` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_of_birth` date NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `height` decimal(5,2) DEFAULT NULL,
  `bmi_value` decimal(5,2) DEFAULT NULL,
  `bmi_category` enum('UNDERWEIGHT','NORMAL','OVERWEIGHT','OBESE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bmi_updated_at` timestamp NULL DEFAULT NULL,
  `emergency_contact_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','PASSIVE','SUSPENDED','ARCHIVED','PENDING','REGISTRATION_FAILED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PASSIVE',
  `failed_attempts` tinyint NOT NULL DEFAULT '0',
  `is_locked` tinyint(1) NOT NULL DEFAULT '0',
  `archived_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_member_phone` (`phone`),
  KEY `idx_member_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'Azra Deneri','2003-09-20','FEMALE','5397069402','azra.deneri@gmail.com','$2a$12$ewG4D6GYUfuduIBu.mQ6l.RhHJSMa7Ou7k6okWfb0ZMbK5U1MSJrq',65.00,173.00,21.72,'NORMAL',NULL,'Nehir Baş','5321549972','ACTIVE',0,0,NULL,'2026-03-21 16:51:00'),(2,'Nehir Baş','2003-11-06','FEMALE','5321549972','nehir.bas@gmail.com','$2a$12$mEODQadR/ytIpvOw0hR4aOQGypStykMubKZS2DuHafdnFFm.32rNq',65.00,169.00,22.76,'NORMAL',NULL,'Azra Deneri','5397069402','ACTIVE',0,0,NULL,'2026-03-21 18:03:54'),(3,'Bengi Su Yarar','2002-10-15','FEMALE','5061719519','bengisuyarar@outlook.com','$2a$12$IKEpgxwkIm30GaJ3oVvyIeMXWfISl/QkKn4SSPew0Xh.vZ4lN2Jsy',53.00,158.00,21.23,'NORMAL',NULL,'Aslı Naz Yarar','5061719520','ACTIVE',0,0,NULL,'2026-03-21 18:14:46'),(4,'Bade Aslan','2005-01-08','FEMALE','5346346608','bade_aslan@hotmail.com','$2a$12$Aq8Wzpj2M.lF9ENmhj8UheqvngK5zYc4uRZfOMR3pQRbWSw0rhn.C',58.00,162.00,22.10,'NORMAL',NULL,'Aslı Naz Yarar','5061719520','ACTIVE',0,0,NULL,'2026-03-21 18:18:21'),(5,'Aslı Naz Yarar','2002-10-15','FEMALE','5061719520','asinaz@gmail.com','$2a$12$5qJXtZHuax4vjYelULvGiePo1QidDKk.gcdSG78KNsA9vGRfh4piq',53.00,160.00,20.70,'NORMAL',NULL,'Azra Deneri','5397069402','ACTIVE',0,0,NULL,'2026-03-21 18:23:13'),(6,'Kemal Aydın','1990-03-12','MALE','5384729163','kemal.aydin@gmail.com','$2a$12$nQg0Q9O64c5.ZyBlOu1kEeiHSKQXoDSAGe4Vj73uz9NZinvl7tQp2',82.00,180.00,25.31,'OVERWEIGHT',NULL,'Fatma Aydın','5321847293','ACTIVE',0,0,NULL,'2026-03-21 21:19:10'),(7,'Hasan Çelik','1988-07-22','MALE','5469012345','hasan.celik@gmail.com','$2a$12$fC5sjRPzje9exkDt0ZcEL0bvn4mI/I804pz7HKjoEYDQggHRbybiq',78.00,178.00,24.62,'NORMAL',NULL,'Ayşe Çelik','5321234567','ARCHIVED',0,0,'2026-03-21 21:32:13','2026-03-21 21:31:51'),(8,'[DELETED]','1900-01-01','MALE','DEL_8',NULL,'$2a$12$fC5sjRPzje9exkDt0ZcEL0bvn4mI/I804pz7HKjoEYDQggHRbybiq',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'ARCHIVED',0,0,'2021-12-31 21:00:00','2026-03-21 21:35:11'),(9,'Çiğdem Demir','1977-03-25','FEMALE','5388799659','cigdemir@gmail.com','$2a$12$R3FCO40aA.ltNMsuewS0becFYk0XHSTrWoy4ULxLO0.W7XUbKcEOC',60.00,160.00,23.44,'NORMAL',NULL,'Azra Deneri','5397069402','ACTIVE',0,0,NULL,'2026-03-22 16:03:55'),(10,'Eda Alemreli','2001-10-25','FEMALE','5313402066','edaalemreli@gmail.com','$2a$12$jRRXu3FSCksTzUUDULTJEOdjSzxt6plx1o25RYv7ouQ8H5E3XK1pW',55.00,165.00,20.20,'NORMAL','2026-03-23 08:19:13','Umut Ateş','5389678132','ACTIVE',0,0,NULL,'2026-03-23 08:17:04');
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membership`
--

DROP TABLE IF EXISTS `membership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership` (
  `membership_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `tier` enum('CLASSIC','GOLD','VIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `package` enum('MONTHLY','ANNUAL_INSTALLMENT','ANNUAL_PREPAID') COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` enum('ACTIVE','PASSIVE','SUSPENDED','FROZEN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `freeze_count` tinyint NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`membership_id`),
  KEY `idx_membership_member` (`member_id`),
  KEY `idx_membership_status` (`status`),
  KEY `idx_membership_end` (`end_date`),
  CONSTRAINT `fk_membership_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership`
--

LOCK TABLES `membership` WRITE;
/*!40000 ALTER TABLE `membership` DISABLE KEYS */;
INSERT INTO `membership` VALUES (1,1,'VIP','MONTHLY','2026-03-21','2026-04-20','ACTIVE',0,'2026-03-21 16:51:00'),(2,2,'VIP','ANNUAL_INSTALLMENT','2026-03-21','2027-03-21','ACTIVE',0,'2026-03-21 18:03:54'),(3,3,'GOLD','ANNUAL_PREPAID','2026-03-21','2027-03-21','ACTIVE',0,'2026-03-21 18:14:46'),(4,4,'CLASSIC','MONTHLY','2026-03-21','2026-04-20','ACTIVE',0,'2026-03-21 18:18:21'),(5,5,'VIP','ANNUAL_PREPAID','2026-03-21','2027-03-28','FROZEN',1,'2026-03-21 18:23:13'),(6,6,'CLASSIC','MONTHLY','2026-03-22','2026-04-21','ACTIVE',0,'2026-03-21 21:21:40'),(7,7,'CLASSIC','MONTHLY','2025-01-01','2025-08-01','PASSIVE',0,'2026-03-21 21:31:53'),(8,1,'GOLD','MONTHLY','2026-03-22','2026-04-21','ACTIVE',0,'2026-03-22 16:05:41'),(9,9,'CLASSIC','MONTHLY','2026-03-22','2026-04-21','ACTIVE',0,'2026-03-22 16:05:44'),(10,10,'VIP','ANNUAL_PREPAID','2026-03-23','2027-03-23','ACTIVE',0,'2026-03-23 08:18:29');
/*!40000 ALTER TABLE `membership` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membership_freeze_log`
--

DROP TABLE IF EXISTS `membership_freeze_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership_freeze_log` (
  `freeze_id` int NOT NULL AUTO_INCREMENT,
  `membership_id` int NOT NULL,
  `freeze_start` date NOT NULL,
  `freeze_end` date NOT NULL,
  `duration_days` tinyint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`freeze_id`),
  KEY `fk_freeze_membership` (`membership_id`),
  CONSTRAINT `fk_freeze_membership` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`membership_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership_freeze_log`
--

LOCK TABLES `membership_freeze_log` WRITE;
/*!40000 ALTER TABLE `membership_freeze_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `membership_freeze_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_type` enum('MEMBERSHIP','INSTALLMENT','EVENT','UPGRADE','MANUAL_CASH') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PAID','PENDING','OVERDUE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `recorded_by` int NOT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `fk_payment_manager` (`recorded_by`),
  KEY `idx_payment_member` (`member_id`),
  KEY `idx_payment_status` (`status`),
  CONSTRAINT `fk_payment_manager` FOREIGN KEY (`recorded_by`) REFERENCES `manager` (`manager_id`),
  CONSTRAINT `fk_payment_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,1,1250.00,'2026-03-21 17:22:48','MEMBERSHIP','GOLD - MONTHLY','PAID',1),(2,2,2140.00,'2026-03-21 18:03:55','MEMBERSHIP','VIP - ANNUAL_INSTALLMENT','PAID',1),(3,3,12750.00,'2026-03-21 18:14:47','MEMBERSHIP','GOLD - ANNUAL_PREPAID','PAID',1),(4,4,750.00,'2026-03-21 18:18:21','MEMBERSHIP','CLASSIC - MONTHLY','PAID',1),(5,5,12750.00,'2026-03-21 18:23:14','MEMBERSHIP','GOLD - ANNUAL_PREPAID','PAID',1),(6,5,9100.00,'2026-03-21 21:09:39','UPGRADE','GOLD → VIP','PAID',3),(7,6,750.00,'2026-03-21 21:21:41','MEMBERSHIP','CLASSIC - MONTHLY','PAID',3),(8,1,750.00,'2026-03-22 16:05:42','MEMBERSHIP','CLASSIC - MONTHLY','PAID',3),(9,9,750.00,'2026-03-22 16:05:44','MEMBERSHIP','CLASSIC - MONTHLY','PAID',3),(10,10,20400.00,'2026-03-23 08:18:29','MEMBERSHIP','VIP - ANNUAL_PREPAID','PAID',3),(11,1,500.00,'2026-03-23 09:40:46','UPGRADE','CLASSIC → GOLD','PAID',3);
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `personal_training_appointment`
--

DROP TABLE IF EXISTS `personal_training_appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personal_training_appointment` (
  `appointment_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `trainer_id` int NOT NULL,
  `appointment_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `status` enum('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SCHEDULED',
  `no_show_penalty_until` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`appointment_id`),
  KEY `idx_apt_member` (`member_id`),
  KEY `idx_apt_trainer_date` (`trainer_id`,`appointment_date`),
  CONSTRAINT `fk_apt_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_apt_trainer` FOREIGN KEY (`trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personal_training_appointment`
--

LOCK TABLES `personal_training_appointment` WRITE;
/*!40000 ALTER TABLE `personal_training_appointment` DISABLE KEYS */;
INSERT INTO `personal_training_appointment` VALUES (3,1,2,'2026-03-16','09:30:00','10:30:00','COMPLETED',NULL,'2026-03-21 20:46:38'),(4,10,5,'2026-03-26','12:11:00','13:11:00','CANCELLED',NULL,'2026-03-23 08:22:20'),(5,10,2,'2026-03-23','09:30:00','10:30:00','SCHEDULED',NULL,'2026-03-23 08:22:47'),(6,10,5,'2026-03-23','11:30:00','12:30:00','SCHEDULED',NULL,'2026-03-23 08:40:31'),(7,10,5,'2026-03-23','13:45:00','14:45:00','SCHEDULED',NULL,'2026-03-23 08:40:34'),(10,10,5,'2026-03-26','12:11:00','13:11:00','CANCELLED',NULL,'2026-03-23 08:46:06'),(11,1,5,'2026-03-23','09:30:00','10:30:00','SCHEDULED',NULL,'2026-03-23 09:41:17');
/*!40000 ALTER TABLE `personal_training_appointment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `registration_request`
--

DROP TABLE IF EXISTS `registration_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registration_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `tier` enum('CLASSIC','GOLD','VIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `package_type` enum('MONTHLY','ANNUAL_INSTALLMENT','ANNUAL_PREPAID') COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` enum('PENDING','APPROVED','FAILED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NOT NULL,
  PRIMARY KEY (`request_id`),
  KEY `fk_rr_member` (`member_id`),
  CONSTRAINT `fk_rr_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registration_request`
--

LOCK TABLES `registration_request` WRITE;
/*!40000 ALTER TABLE `registration_request` DISABLE KEYS */;
INSERT INTO `registration_request` VALUES (1,6,'CLASSIC','MONTHLY',750.00,'APPROVED','2026-03-21 21:21:04','2026-03-24 21:21:05'),(2,1,'CLASSIC','MONTHLY',750.00,'APPROVED','2026-03-21 22:01:14','2026-03-24 22:01:14'),(3,9,'CLASSIC','MONTHLY',750.00,'APPROVED','2026-03-22 16:03:55','2026-03-25 16:03:56'),(4,10,'VIP','ANNUAL_PREPAID',20400.00,'APPROVED','2026-03-23 08:17:04','2026-03-26 08:17:05');
/*!40000 ALTER TABLE `registration_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tier_upgrade_request`
--

DROP TABLE IF EXISTS `tier_upgrade_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tier_upgrade_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `membership_id` int NOT NULL,
  `current_tier` enum('CLASSIC','GOLD','VIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_tier` enum('CLASSIC','GOLD','VIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `upgrade_fee` decimal(10,2) NOT NULL,
  `status` enum('PENDING','APPROVED','FAILED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NOT NULL,
  PRIMARY KEY (`request_id`),
  KEY `fk_tur_member` (`member_id`),
  KEY `fk_tur_membership` (`membership_id`),
  CONSTRAINT `fk_tur_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_tur_membership` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`membership_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tier_upgrade_request`
--

LOCK TABLES `tier_upgrade_request` WRITE;
/*!40000 ALTER TABLE `tier_upgrade_request` DISABLE KEYS */;
INSERT INTO `tier_upgrade_request` VALUES (1,5,5,'GOLD','VIP',9100.00,'APPROVED','2026-03-21 21:09:02','2026-03-24 21:09:02'),(2,1,8,'CLASSIC','GOLD',500.00,'APPROVED','2026-03-22 16:28:53','2026-03-25 16:28:54'),(3,1,8,'CLASSIC','GOLD',483.33,'FAILED','2026-03-23 09:40:12','2026-03-26 09:40:13');
/*!40000 ALTER TABLE `tier_upgrade_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer`
--

DROP TABLE IF EXISTS `trainer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer` (
  `trainer_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `failed_attempts` tinyint NOT NULL DEFAULT '0',
  `is_locked` tinyint(1) NOT NULL DEFAULT '0',
  `specialty` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`trainer_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer`
--

LOCK TABLES `trainer` WRITE;
/*!40000 ALTER TABLE `trainer` DISABLE KEYS */;
INSERT INTO `trainer` VALUES (1,'Mehmet Yılmaz','mehmetyilmaz','$2a$12$j2H5wkDym2ponAKwAkGy6OHHgJ0tJHPrn3cbsu5w07tsfvac1pCyK',0,0,'Pilates',1,'2026-03-21 19:23:18'),(2,'Ali Yıldırım','aliyıldırım','$2a$12$EVCfYiF9xhvovbJ/8AYGZOyRC8kCMBTEemy84uD159YXyJpL3ssjG',0,0,'Muscle Gain',1,'2026-03-21 19:24:24'),(3,'Sevda Şahin','sevdasahin','$2a$12$Pp4PXCzijjvBMr5y1rml/uZX8aqd9Y0dyIEbUyq92QEtHeuq8Jhra',0,0,'Women\'s Fitness',1,'2026-03-21 19:24:42'),(4,'Egemen Yıldız','egemenyildiz','$2a$12$ZQKaL0owNGUeyuMRX3dXoukmF7kKADFdSuJlkXExakIZPuO3UI2Qm',0,0,'HIIT Training',1,'2026-03-21 19:25:13'),(5,'Leyla Özer','leylaozer','$2a$12$SsI2BrPz3Qc7tVi9uUtzsu4F9p0/lSDGjJ/WFYzKqPjZiqyfeNqp2',0,0,'Yoga',1,'2026-03-21 19:25:35');
/*!40000 ALTER TABLE `trainer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer_leave_request`
--

DROP TABLE IF EXISTS `trainer_leave_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer_leave_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `trainer_id` int NOT NULL,
  `leave_date` date NOT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `leave_start` date DEFAULT NULL,
  `leave_end` date DEFAULT NULL,
  PRIMARY KEY (`request_id`),
  KEY `fk_lr_trainer` (`trainer_id`),
  CONSTRAINT `fk_lr_trainer` FOREIGN KEY (`trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer_leave_request`
--

LOCK TABLES `trainer_leave_request` WRITE;
/*!40000 ALTER TABLE `trainer_leave_request` DISABLE KEYS */;
INSERT INTO `trainer_leave_request` VALUES (1,2,'2026-03-26','Sickness','APPROVED','2026-03-22 16:30:50','2026-03-26','2026-03-26'),(2,2,'2026-06-01','Vacation','APPROVED','2026-03-23 08:24:08','2026-06-01','2026-06-01'),(3,3,'2026-03-30','Vacation','REJECTED','2026-03-23 09:38:54','2026-03-30','2026-04-04');
/*!40000 ALTER TABLE `trainer_leave_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer_lesson_slots`
--

DROP TABLE IF EXISTS `trainer_lesson_slots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer_lesson_slots` (
  `slot_id` int NOT NULL AUTO_INCREMENT,
  `trainer_id` int NOT NULL,
  `day_of_week` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  PRIMARY KEY (`slot_id`),
  KEY `fk_slot_trainer` (`trainer_id`),
  CONSTRAINT `fk_slot_trainer` FOREIGN KEY (`trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer_lesson_slots`
--

LOCK TABLES `trainer_lesson_slots` WRITE;
/*!40000 ALTER TABLE `trainer_lesson_slots` DISABLE KEYS */;
INSERT INTO `trainer_lesson_slots` VALUES (1,5,'MONDAY','09:30:00','10:30:00'),(2,5,'MONDAY','11:30:00','12:30:00'),(3,5,'MONDAY','13:45:00','14:45:00'),(4,5,'THURSDAY','12:11:00','13:11:00'),(5,2,'MONDAY','09:30:00','10:30:00'),(6,4,'MONDAY','09:30:00','10:30:00'),(7,4,'MONDAY','11:30:00','12:30:00');
/*!40000 ALTER TABLE `trainer_lesson_slots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer_working_days`
--

DROP TABLE IF EXISTS `trainer_working_days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer_working_days` (
  `id` int NOT NULL AUTO_INCREMENT,
  `trainer_id` int NOT NULL,
  `day_of_week` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_trainer_day` (`trainer_id`,`day_of_week`),
  CONSTRAINT `fk_wd_trainer` FOREIGN KEY (`trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer_working_days`
--

LOCK TABLES `trainer_working_days` WRITE;
/*!40000 ALTER TABLE `trainer_working_days` DISABLE KEYS */;
INSERT INTO `trainer_working_days` VALUES (1,2,'MONDAY','09:00:00','18:00:00'),(2,2,'TUESDAY','09:00:00','18:00:00'),(3,2,'WEDNESDAY','09:00:00','18:00:00'),(4,2,'THURSDAY','09:00:00','18:00:00'),(5,2,'FRIDAY','09:00:00','18:00:00'),(6,4,'MONDAY','09:00:00','16:00:00'),(7,4,'WEDNESDAY','09:00:00','18:00:00'),(8,4,'FRIDAY','09:00:00','19:00:00'),(9,5,'MONDAY','09:00:00','16:00:00'),(10,5,'THURSDAY','09:00:00','19:00:00'),(11,5,'FRIDAY','09:00:00','17:00:00'),(12,5,'SATURDAY','09:00:00','18:00:00'),(13,5,'SUNDAY','09:00:00','18:00:00'),(14,1,'TUESDAY','09:00:00','18:00:00'),(15,1,'THURSDAY','09:00:00','18:00:00'),(16,1,'FRIDAY','09:00:00','18:00:00'),(17,1,'SATURDAY','09:00:00','18:00:00'),(18,3,'MONDAY','09:00:00','18:00:00'),(19,3,'TUESDAY','09:00:00','18:00:00'),(20,3,'WEDNESDAY','09:00:00','18:00:00'),(21,3,'SATURDAY','09:00:00','18:00:00'),(22,3,'SUNDAY','09:00:00','18:00:00');
/*!40000 ALTER TABLE `trainer_working_days` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-23 13:12:30
