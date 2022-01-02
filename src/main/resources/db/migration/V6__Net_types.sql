ALTER TABLE switches
ALTER COLUMN ip TYPE inet USING ip::inet,
    ALTER COLUMN mac TYPE macaddr USING mac::macaddr;