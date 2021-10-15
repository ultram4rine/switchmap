CREATE TABLE IF NOT EXISTS builds (
    name TEXT NOT NULL UNIQUE,
    short_name TEXT NOT NULL UNIQUE,
    PRIMARY KEY (name, short_name)
);
CREATE TABLE IF NOT EXISTS floors (
    number INT NOT NULL,
    build_name TEXT NOT NULL,
    build_short_name TEXT NOT NULL,
    PRIMARY KEY (build_short_name, number),
    CONSTRAINT fk_build FOREIGN KEY (build_name, build_short_name) REFERENCES builds (name, short_name) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS switches (
    name TEXT NOT NULL UNIQUE,
    ip INET NOT NULL UNIQUE,
    mac VARCHAR(12) NOT NULL UNIQUE,
    snmp_community TEXT NOT NULL,
    revision TEXT NULL,
    serial TEXT NULL UNIQUE,
    ports_number INT NULL,
    build_short_name TEXT NULL,
    floor_number INT NULL,
    position_top DECIMAL NULL,
    position_left DECIMAL NULL,
    up_switch_name TEXT NULL,
    up_switch_mac VARCHAR(12) NULL,
    up_link TEXT NULL,
    PRIMARY KEY (name, mac),
    CONSTRAINT fk_floor FOREIGN KEY (build_short_name, floor_number) REFERENCES floors (build_short_name, number) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT fk_switch FOREIGN KEY (up_switch_name, up_switch_mac) REFERENCES switches (name, mac) ON UPDATE CASCADE ON DELETE NO ACTION
);