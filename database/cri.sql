-- ms_schema.critical_table_tx definition

-- Drop table

-- DROP TABLE ms_schema.critical_table_tx;

CREATE TABLE ms_schema.critical_table_tx (
	pkey varchar NOT NULL,
	userid varchar NOT NULL,
	isactive bool NOT NULL DEFAULT true
);
