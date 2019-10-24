#!/bin/bash

go build switchmap.go
mv -f switchmap /var/www/switchmap/
cp -r switchmap.conf.json public/ private/ templates/ /var/www/switchmap/
systemctl restart switchmap