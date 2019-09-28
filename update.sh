#!/bin/bash

go build switchmap.go
mv -f switchmap /var/www/switchmap/
cp -r conf.json public/ private/ /var/www/switchmap/
systemctl restart switchmap