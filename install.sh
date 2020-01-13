#!/bin/bash

cp switchmap.service /etc/systemd/system/
systemctl daemon-reload
go build switchmap.go
mv -f switchmap /var/www/switchmap/
cp -r switchmap.conf.toml schema.sql public/ private/ templates/ /var/www/switchmap/
(cd /var/www/switchmap/public; npm install)
systemctl enable switchmap
systemctl start switchmap