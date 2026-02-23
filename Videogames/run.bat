@echo off
echo 🎮 Gestion de Videojuegos - Iniciando...
echo.

REM Crear directorio para BD H2
if not exist "data" mkdir data

REM Compilar si no existe el JAR
set JAR=target\videogames-app-1.0-SNAPSHOT-jar-with-dependencies.jar
if not exist "%JAR%" (
    echo Compilando proyecto...
    call mvn clean package -q
    if errorlevel 1 (
        echo ERROR: Compilacion fallida. Revisa los errores.
        pause
        exit /b 1
    )
    echo Compilacion exitosa
)

echo.
echo Ejecutando aplicacion...
echo.
java -jar "%JAR%"
pause
