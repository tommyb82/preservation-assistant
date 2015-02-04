-- # Initialise Preservation Assistant schema
-- # HyperSQL DataBase. For development use only!
-- # Author Tom Bunting

DROP TABLE user_role IF EXISTS;
DROP TABLE users IF EXISTS;
DROP TABLE roles IF EXISTS;
DROP TABLE data_holder_metadata IF EXISTS;
DROP TABLE form_categories IF EXISTS;
DROP TABLE form_field IF EXISTS;
DROP TABLE form IF EXISTS;
DROP TABLE dataset_ril IF EXISTS;
DROP TABLE form_bundle IF EXISTS;
DROP TABLE registry_auth IF EXISTS;

CREATE TABLE form_bundle (
	dataset_name	VARCHAR(32) PRIMARY KEY,
	bundle_name		VARCHAR(64),
	processor_name	VARCHAR(64) NOT NULL,
	display_name 	VARCHAR(64)
);

CREATE TABLE dataset_ril (
	ril_cpid			VARCHAR(128) PRIMARY KEY,
	dataset_name		VARCHAR(32), -- Should be not null but eclipselink uni-dir 1:m bug means we must leave it null.
	ril_name			VARCHAR(128) NOT NULL,
	ril					VARCHAR(4096) NOT NULL, -- persisted serialized RIL in XML form
	preserved			BOOLEAN DEFAULT FALSE NOT NULL,
	CONSTRAINT dataset_rilname UNIQUE (dataset_name, ril_name),
	CONSTRAINT fk_dataset_ril_dataset FOREIGN KEY (dataset_name) REFERENCES form_bundle (dataset_name)
);

CREATE TABLE form (
	form_id				INTEGER IDENTITY,
	dataset_name		VARCHAR(32) NOT NULL,
	form_name			VARCHAR(128) NOT NULL,
	form_type			VARCHAR(32) NOT NULL,
	form_group			VARCHAR(32) NOT NULL,
	group_order			INTEGER,
	display_name 		VARCHAR(128) NOT NULL,
	item_file_name		VARCHAR(128) NULL,
	intro_text			VARCHAR(512),
	data_holder			VARCHAR(20971520), -- 20MB max
	data_holder_type	VARCHAR(64) NOT NULL,
	ril_cpid			VARCHAR(128) NOT NULL,
	manifest_cpid		VARCHAR(128) NULL,
	preserved			BOOLEAN DEFAULT FALSE NOT NULL,
	CONSTRAINT fk_form_form_bundle FOREIGN KEY (dataset_name) REFERENCES form_bundle (dataset_name),
	CONSTRAINT form_unique UNIQUE (dataset_name, form_name, item_file_name)
);

CREATE TABLE form_field (
	field_id		INTEGER IDENTITY,
	form_id			INTEGER NOT NULL,
	field_value		VARCHAR(1024),
	display_name 	VARCHAR(64) NOT NULL,
	help_text		VARCHAR(512),
	default_value	VARCHAR(64),
	max_value		VARCHAR(64),
	min_value		VARCHAR(64),
	CONSTRAINT fk_form_field_form FOREIGN KEY (form_id) REFERENCES form (form_id)
);

CREATE TABLE data_holder_metadata (
	form_id			INTEGER NOT NULL,
	metadata_key	VARCHAR(32) NOT NULL,
	metadata_value	VARCHAR(256) NOT NULL,
	PRIMARY KEY (form_id, metadata_key),
	CONSTRAINT fk_data_holder_metadata_form FOREIGN KEY (form_id) REFERENCES form (form_id)
);

CREATE TABLE form_categories (
	form_id			INTEGER NOT NULL,
	category_name	VARCHAR(256) NOT NULL,
	PRIMARY KEY (form_id, category_name),
	CONSTRAINT fk_categories_form FOREIGN KEY (form_id) REFERENCES form (form_id)
);

CREATE TABLE registry_auth (
	registry_uid 	VARCHAR(32) PRIMARY KEY,
	principal		VARCHAR(64) NOT NULL,
	credential		VARCHAR(64) NOT NULL,
	updated_by		VARCHAR(32) NOT NULL, -- Application user ID
	ts 				TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security schema
CREATE TABLE users (
    username	VARCHAR(32) NOT NULL PRIMARY KEY,
    password	VARCHAR(128) NOT NULL,
    enabled		BOOLEAN DEFAULT TRUE NOT NULL,
    ts			TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles(
	role 		VARCHAR(32) NOT NULL PRIMARY KEY,
	description VARCHAR(512) NULL,
	enabled		BOOLEAN DEFAULT TRUE NOT NULL,
	ts			TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    username	VARCHAR(16) NOT NULL,
    role 		VARCHAR(32) NOT NULL,
    ts			TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(username, role),
    CONSTRAINT fk_user_role_users FOREIGN KEY(username) REFERENCES users(username),
    CONSTRAINT fk_user_role_roles FOREIGN KEY(role) REFERENCES roles(role)
);