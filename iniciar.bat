@echo off
title FinTrack

if not exist target\fintrack.jar (
    echo =========================================
    echo   REALIZANDO A COMPILACAO DO PROJETO...
    echo =========================================
    echo.
    echo Essa etapa demora um pouquinho, mas so acontece na 
    echo primeira vez ou quando o codigo e modificado.
    echo Aguarde...
    call mvnw package -DskipTests
)

:: javaw abre o aplicativo sem manter a janela preta aberta no fundo
start "" javaw -jar target\fintrack.jar
