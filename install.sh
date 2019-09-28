#!/bin/bash

cp switchmap.service /etc/systemd/system/
systemctl daemon-reload
go build switchmap.go
mv -f switchmap /var/www/switchmap/
cp -r conf.json public/ private/ templates/ /var/www/switchmap/
systemctl enable switchmap
systemctl start switchmap