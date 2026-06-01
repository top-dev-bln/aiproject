@echo off
REM Environment setup script for Job Recommendation System
REM Sets up necessary environment variables for the application

echo Setting up Job Recommendation System environment variables...

REM Database Configuration
echo Setting database environment variables...
set /p DB_HOST="Enter database host (default: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="Enter database port (default: 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_NAME="Enter database name (default: jobrec): "
if "%DB_NAME%"=="" set DB_NAME=jobrec

set /p DB_USERNAME="Enter database username (default: admin): "
if "%DB_USERNAME%"=="" set DB_USERNAME=admin

set /p DB_PASSWORD="Enter database password: "

REM Redis Configuration
echo Setting Redis environment variables...
set /p REDIS_HOST="Enter Redis host (default: localhost): "
if "%REDIS_HOST%"=="" set REDIS_HOST=localhost

set /p REDIS_PORT="Enter Redis port (default: 6379): "
if "%REDIS_PORT%"=="" set REDIS_PORT=6379

set /p REDIS_PASSWORD="Enter Redis password (optional): "

REM API Keys
echo Setting API key environment variables...
set /p SERPAPI_KEY="Enter SerpAPI key: "
set /p EDENAI_KEY="Enter EdenAI key: "

REM Application Environment
set /p APP_ENV="Enter application environment (dev/prod, default: dev): "
if "%APP_ENV%"=="" set APP_ENV=dev

echo.
echo Environment variables set successfully!
echo.
echo Database: %DB_USERNAME%@%DB_HOST%:%DB_PORT%/%DB_NAME%
echo Redis: %REDIS_HOST%:%REDIS_PORT%
echo Environment: %APP_ENV%
echo.

REM Create environment file for persistence
echo Creating .env file...
(
echo # Job Recommendation System Environment Variables
echo # Generated on %DATE% %TIME%
echo.
echo # Database Configuration
echo DB_HOST=%DB_HOST%
echo DB_PORT=%DB_PORT%
echo DB_NAME=%DB_NAME%
echo DB_USERNAME=%DB_USERNAME%
echo DB_PASSWORD=%DB_PASSWORD%
echo.
echo # Redis Configuration
echo REDIS_HOST=%REDIS_HOST%
echo REDIS_PORT=%REDIS_PORT%
echo REDIS_PASSWORD=%REDIS_PASSWORD%
echo.
echo # API Keys
echo SERPAPI_KEY=%SERPAPI_KEY%
echo EDENAI_KEY=%EDENAI_KEY%
echo.
echo # Application Configuration
echo APP_ENVIRONMENT=%APP_ENV%
) > .env

echo Environment file created: .env
echo.
echo IMPORTANT: Keep the .env file secure and do not commit it to version control!
echo.
pause
