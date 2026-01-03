@echo off
REM Script para configurar las variables de entorno de la base de datos en Windows
REM Ejecutar con: setup-env.bat

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=mi_roma
set DB_USER=mi_roma_app
set DB_PASSWORD=MiRomaApp_2025!

echo Variables de entorno configuradas:
echo DB_HOST=%DB_HOST%
echo DB_PORT=%DB_PORT%
echo DB_NAME=%DB_NAME%
echo DB_USER=%DB_USER%
echo DB_PASSWORD=***

