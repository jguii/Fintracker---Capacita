@echo off
title FinTrack

echo =========================================
echo         INICIANDO O FINTRACK...
echo =========================================
echo.
echo Por favor, aguarde enquanto o aplicativo e carregado.
echo (Esta janela de comando pode ser fechada apos o uso)
echo.

call mvn javafx:run

:: Se houver erro, pausa para o usuario poder ler
if %errorlevel% neq 0 (
    echo.
    echo Ocorreu um erro ao iniciar o aplicativo. 
    echo Verifique se o Maven (mvn) esta instalado e configurado corretamente.
    pause
)
