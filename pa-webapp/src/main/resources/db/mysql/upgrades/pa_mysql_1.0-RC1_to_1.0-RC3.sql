-- # Upgrade script for Preservation Assistant schema
-- # Version 1.0-RC2 to 1.0-RC-3 

USE paw;
ALTER TABLE form ADD COLUMN item_file_name VARCHAR(128) NULL;
COMMIT;