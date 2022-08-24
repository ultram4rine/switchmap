ALTER TABLE floors DROP CONSTRAINT fk_build;
ALTER TABLE floors DROP COLUMN build_name;
ALTER TABLE builds DROP CONSTRAINT builds_pkey;
ALTER TABLE builds
ADD PRIMARY KEY (short_name);
ALTER TABLE floors
ADD FOREIGN KEY (build_short_name) REFERENCES builds (short_name) ON UPDATE CASCADE ON DELETE CASCADE;