@echo off
title KubeCKA Alert Service (Microservicio de Alertas SRE - Port 8082)
cd /d "%~dp0"
echo ===============================================================
echo   INICIANDO MICROSERVICIO: KubeCKA Alert Service (Port 8082)
echo   Consumiendo eventos desde RabbitMQ: k8s.incidents.queue
echo ===============================================================
mvn spring-boot:run
pause
