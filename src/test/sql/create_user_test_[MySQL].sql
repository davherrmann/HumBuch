CREATE USER 'travis'@'%' 
IDENTIFIED BY '';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER 
ON humbuch_test.*
TO 'travis'@'%';