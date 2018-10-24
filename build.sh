#!/bin/bash
mvn clean package
vim application.properties
docker build --tag dashboard .