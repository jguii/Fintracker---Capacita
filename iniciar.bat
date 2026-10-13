@echo off
title FinTrack

:: Se o executavel nao existir ou se o usuario passar o parametro /r, compila o projeto
if "%1"=="/r" (
    echo Forcando recompilacao do projeto...
    del /q target\fintrack.jar 2>nul
)

if not exist target\fintrack.jar (
    echo =========================================
    echo   REALIZANDO A COMPILACAO DO PROJETO...
    echo =========================================
    echo.
    echo Essa etapa demora um pouquinho, mas so acontece na 
    echo primeira vez ou quando o codigo for recompilado.
    echo Aguarde...
    call mvnw.cmd package -DskipTests
    if errorlevel 1 (
        echo.
        echo =========================================
        echo   ERRO: Falha na compilacao do projeto!
        echo =========================================
        echo Verifique o log acima para identificar o problema.
        echo.
        pause
        exit /b 1
    )
)

if not exist target\fintrack.jar (
    echo.
    echo Arquivo target\fintrack.jar nao foi encontrado.
    pause
    exit /b 1
)

:: javaw abre o aplicativo sem manter a janela preta aberta no fundo
start "" javaw -jar target\fintrack.jar
