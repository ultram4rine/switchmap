#!/bin/bash

go build switchmap.go
mv -f switchmap /var/www/switchmap/
cp -r switchmap.conf.json public/ private/ templates/ /var/www/switchmap/
(cd /var/www/switchmap/public/js/; npm install)
systemctl restart switchmap