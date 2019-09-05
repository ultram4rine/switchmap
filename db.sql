CREATE DATABASE IF NOT EXISTS switchmap;
use switchmap;

CREATE TABLE IF NOT EXISTS switches (
	id SERIAL PRIMARY KEY,
    name STRING NOT NULL,
    ip INET NOT NULL,
    mac STRING NOT NULL,
    revision STRING NULL,
    serial STRING NULL,
    model STRING NULL,
    build STRING NOT NULL,
    floor STRING NOT NULL,
    upswitch STRING NULL,
    postop STRING NULL,
    posleft STRING NULL
);

create table buildings (
	id SERIAL PRIMARY KEY,
	name STRING NOT NULL,
    addr STRING NOT NULL
);

create table floors (
	id SERIAL PRIMARY KEY,
	build STRING NOT NULL,
    floor STRING NOT NULL
);
