#!/bin/bash

# Script para configurar las variables de entorno de la base de datos
# Ejecutar con: source setup-env.sh

export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=mi_roma
export DB_USER=mi_roma_app
export DB_PASSWORD=MiRomaApp_2025!

echo "Variables de entorno configuradas:"
echo "DB_HOST=$DB_HOST"
echo "DB_PORT=$DB_PORT"
echo "DB_NAME=$DB_NAME"
echo "DB_USER=$DB_USER"
echo "DB_PASSWORD=***"

