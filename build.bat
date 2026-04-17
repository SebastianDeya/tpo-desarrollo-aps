@echo off
REM Script para compilar el proyecto Truco Counter
REM Ejecutar desde PowerShell como administrador o desde CMD

cd /d "%~dp0"

echo Limpiando proyecto...
if exist ".gradle" rd /s /q ".gradle" 2>nul
if exist "build" rd /s /q "build" 2>nul
if exist "app\build" rd /s /q "app\build" 2>nul

echo Descargando dependencias e inicializando Gradle...
REM Si no tienes gradlew, descárgalo del siguiente comando:
REM Pero primero, intenta usar gradle del PATH si existe

REM Opción 1: Si tienes Gradle instalado globalmente
if exist "gradlew.bat" (
    echo Encontrado gradlew.bat
    call gradlew.bat clean build
) else if exist "gradlew" (
    echo Encontrado gradlew (Unix)
    bash gradlew clean build
) else (
    echo gradlew no encontrado. Necesitas inicializar el wrapper o tener Gradle instalado.
    echo Para inicializar el wrapper, abre Android Studio y permite que configure el proyecto.
    pause
)

echo Compilación completada.
pause

