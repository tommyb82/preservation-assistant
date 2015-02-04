-- # Initialise Preservation Assistant schema
-- # MySQL. For production use.
-- # 
-- # Instructions:
-- # 1) Optional: change the 'paw' user password below (default 'pawpaw'), before starting. 
-- # 2) Run this script as root (or other privileged account)
-- # 3) Change the 'paw' account password, if not done in step 1
-- # 4) Increase the MySQL 'max_allowed_packet' setting to at least 10MB 
-- # 5) Update $WEBAPPS/pa-webapp/WEB-INF/classes/db/data-access.properties with your MySQL connection details
-- #
-- # Author Tom Bunting

-- DROP DATABASE IF EXISTS paw;

CREATE DATABASE IF NOT EXISTS paw;
GRANT ALL PRIVILEGES ON paw.* TO paw@localhost IDENTIFIED BY 'pawpaw';

USE paw;

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS data_holder_metadata;
DROP TABLE IF EXISTS form_categories;
DROP TABLE IF EXISTS form_field;
DROP TABLE IF EXISTS form;
DROP TABLE IF EXISTS dataset_ril;
DROP TABLE IF EXISTS form_bundle;
DROP TABLE IF EXISTS registry_auth;

CREATE TABLE IF NOT EXISTS form_bundle (
	dataset_name	VARCHAR(32) PRIMARY KEY,
	bundle_name		VARCHAR(64),
	display_name 	VARCHAR(64),
	processor_name	VARCHAR(64) NOT NULL,
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS dataset_ril (
	ril_cpid		VARCHAR(128) PRIMARY KEY,
	dataset_name	VARCHAR(32), -- Should be not null but eclipselink uni-dir 1:m bug means we must leave it null.
	ril_name		VARCHAR(128) NOT NULL,
	ril				VARCHAR(4096) NOT NULL, -- persisted serialized RIL in XML form
	preserved		TINYINT(1) DEFAULT 0 NOT NULL,
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT dataset_rilname_unique UNIQUE (dataset_name, ril_name),
	FOREIGN KEY (dataset_name) REFERENCES form_bundle (dataset_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS form (
	form_id				INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,	
	dataset_name		VARCHAR(32) NOT NULL,
	form_name			VARCHAR(128) NOT NULL,
	form_type			VARCHAR(32) NOT NULL,
	form_group			VARCHAR(32) NOT NULL,
	group_order			INTEGER UNSIGNED NOT NULL,
	display_name 		VARCHAR(128) NOT NULL,
	item_file_name		VARCHAR(128) NULL,
	intro_text			VARCHAR(512),
	data_holder			LONGTEXT,
	data_holder_type	VARCHAR(64) NOT NULL,
	ril_cpid			VARCHAR(128) NOT NULL,
	manifest_cpid		VARCHAR(128) NULL,
	preserved			TINYINT(1) DEFAULT 0 NOT NULL,
	ts 					TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (dataset_name) REFERENCES form_bundle (dataset_name),
	CONSTRAINT form_unique UNIQUE (dataset_name, form_name, item_file_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS form_field (
	field_id		INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,	
	form_id			INTEGER UNSIGNED NOT NULL,
	field_value		MEDIUMTEXT,
	display_name 	VARCHAR(64) NOT NULL,
	help_text		VARCHAR(512),
	default_value	VARCHAR(64),
	max_value		VARCHAR(64),
	min_value		VARCHAR(64),
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (form_id) REFERENCES form (form_id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS data_holder_metadata (
	form_id			INTEGER UNSIGNED NOT NULL,
	metadata_key	VARCHAR(32) NOT NULL,
	metadata_value	VARCHAR(256) NOT NULL,
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY 	(form_id, metadata_key),
	FOREIGN KEY 	(form_id) REFERENCES form (form_id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS form_categories (
	form_id			INTEGER UNSIGNED NOT NULL,
	category_name	VARCHAR(256) NOT NULL,
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY 	(form_id, category_name),
	FOREIGN KEY 	(form_id) REFERENCES form (form_id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS registry_auth (
	registry_uid 	VARCHAR(32) PRIMARY KEY,
	principal		VARCHAR(64) NOT NULL,
	credential		VARCHAR(64) NOT NULL,
	updated_by		VARCHAR(32) NOT NULL, -- Application user ID
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) engine=InnoDB;

-- # Security schema
CREATE TABLE IF NOT EXISTS users (
    username	VARCHAR(32) PRIMARY KEY,
    password	VARCHAR(128) NOT NULL,
    enabled		TINYINT(1) DEFAULT 1 NOT NULL,
    ts			TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS roles(
	role 		VARCHAR(32) PRIMARY KEY,
	description VARCHAR(512) NULL,
	enabled		TINYINT(1) DEFAULT 1 NOT NULL,
	ts			TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS user_role (
    username	VARCHAR(16) NOT NULL,
    role 		VARCHAR(32) NOT NULL,
    ts			TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY	(username, role),
    FOREIGN KEY	(username) REFERENCES users (username),
    FOREIGN KEY	(role) REFERENCES roles (role)
) engine=InnoDB;

-- # Populate security schema
INSERT INTO roles (ROLE, DESCRIPTION ) VALUES ( 'ROLE_USER', 'Normal user account');
INSERT INTO roles ( ROLE, DESCRIPTION ) VALUES ( 'ROLE_ADMIN', 'Admin level access');

-- Default password: adm1n
INSERT INTO users ( USERNAME, PASSWORD ) VALUES ( 'admin', '$2a$10$408HsqMnj5BKOvg1w3aluOxG8YDgwLsvHri8EY5zckztyYAu.Or52');
-- Default password: pr3s3rv4tr0n
INSERT INTO users ( USERNAME, PASSWORD ) VALUES ( 'p3kuser', '$2a$10$P5JntEaSSYjmexfkA8uCWe/.Vr1Q6voK18kb3NEk.tte5UhhgrY2W');

INSERT INTO user_role ( USERNAME, ROLE ) VALUES ( 'admin', 'ROLE_USER' );
INSERT INTO user_role ( USERNAME, ROLE ) VALUES ( 'admin', 'ROLE_ADMIN' );
INSERT INTO user_role ( USERNAME, ROLE ) VALUES ( 'p3kuser', 'ROLE_USER' );
