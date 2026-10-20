@echo off
REM Can JDK 17 va ket noi mang (lan dau tai Forge ~ vai tram MB)
call gradlew.bat build
echo.
echo File jar nam o: build\libs\ldhquiz-1.0.0.jar
pause
