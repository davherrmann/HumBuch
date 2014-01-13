-- -----------------------------------------------------
-- Data for table `humbuch_test`.`grade`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`grade` (`id`, `grade`, `suffix`, `teacher`) VALUES (1, 5, 'a', NULL);
INSERT INTO `humbuch_test`.`grade` (`id`, `grade`, `suffix`, `teacher`) VALUES (2, 5, 'b', NULL);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`parent`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`parent` (`id`, `title`, `firstname`, `lastname`, `street`, `postcode`, `city`) VALUES (1, 'Herr', 'Siegfried', 'Müller', 'Musterstraße 1', 12345, 'Musterstadt');

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`student`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`student` (`id`, `gradeId`, `lastname`, `firstname`, `birthday`, `gender`, `parentId`, `leavingSchool`) VALUES (1, 1, 'Müller', 'Hans', '1998-05-01', 'm', 1, 0);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`category`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`category` (`id`, `name`, `description`) VALUES (1, 'Bücher', NULL);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`teachingMaterial`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`teachingMaterial` (`id`, `categoryId`, `name`, `identifyingNumber`, `producer`, `price`, `comment`, `fromGrade`, `fromTerm`, `toGrade`, `toTerm`, `validFrom`, `validUntil`) VALUES (1, 1, 'Englisch Buch', '123456789', NULL, 0.00, NULL, 5, 1, 5, 2, '2013-12-01', NULL);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`borrowedMaterial`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`borrowedMaterial` (`id`, `studentId`, `teachingMaterialId`, `borrowFrom`, `borrowUntil`, `returnDate`, `received`, `defect`, `defectComment`) VALUES (1, 1, 1, '2013-12-04', '2014-07-31', NULL, 0, 0, NULL);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`user`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`user` (`id`, `username`, `password`, `email`) VALUES (1, 'admin', '1234', 'test@test.de');

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`role`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`role` (`id`, `name`, `description`) VALUES (1, 'Admin', NULL);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`permission`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`permission` (`id`, `name`, `description`) VALUES (1, 'darf alles', NULL);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`user_has_role`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`user_has_role` (`user_id`, `role_id`) VALUES (1, 1);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`role_has_permission`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`role_has_permission` (`role_id`, `permission_id`) VALUES (1, 1);

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`schoolYear`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`schoolYear` (`year`, `from`, `to`, `endFirstTerm`, `beginSecondTerm`) VALUES (2013, '2013-08-01', '2014-07-31', '2014-01-31', '2014-02-01');

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`studentSubject`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`studentSubject` (`id`, `studentId`, `subject`) VALUES (1, 1, 'ENGLISH');
INSERT INTO `humbuch_test`.`studentSubject` (`id`, `studentId`, `subject`) VALUES (2, 1, 'LATIN');

COMMIT;


-- -----------------------------------------------------
-- Data for table `humbuch_test`.`teachingMaterialSubject`
-- -----------------------------------------------------
START TRANSACTION;
USE `humbuch_test`;
INSERT INTO `humbuch_test`.`teachingMaterialSubject` (`id`, `teachingMaterialId`, `subject`) VALUES (1, 1, 'ENGLISH');

COMMIT;
