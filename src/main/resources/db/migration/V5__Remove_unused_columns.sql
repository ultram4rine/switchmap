ALTER TABLE switches DROP COLUMN ports_number;
ALTER TABLE switches DROP CONSTRAINT fk_switch;
ALTER TABLE switches DROP COLUMN up_switch_mac;