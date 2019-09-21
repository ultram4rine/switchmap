#!/bin/bash

go build switchmap.go
rm /var/www/switchmap/switchmap
cp -r switchmap conf.json public/ private/ /var/www/switchmap/