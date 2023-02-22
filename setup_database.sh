#!/usr/bin/env bash

# Simple script to initialize the database for both killbill and kaui

export PGPASSWORD=adminpwd
psql -h localhost -U admin -d postgres -f killbill-db/setup_postgresql.sql
psql -h localhost -U admin -d killbill -f killbill-db/setup_domains.sql
export PGPASSWORD=kbadminpwd
psql -h localhost -U kbadmin -d killbill -f killbill-db/killbill_ddl.sql
export PGPASSWORD=kauiadminpwd
psql -h localhost -U kauiadmin -d killbill -f killbill-db/kaui_ddl.sql
echo "Killbill and Kaui schemas initialized."