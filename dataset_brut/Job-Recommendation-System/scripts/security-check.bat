@echo off
REM Security check script for Job Recommendation System
REM Author: Leo Ji
REM This script checks for potential security issues before deployment

echo ========================================
echo Job Recommendation System Security Check
echo Author: Leo Ji
echo ========================================
echo.

REM Check for hardcoded API keys
echo [1/6] Checking for hardcoded API keys...
findstr /S /I /C:"api_key.*=" "Source Code\*.java" > nul
if %ERRORLEVEL% EQU 0 (
    echo ❌ WARNING: Potential hardcoded API keys found!
    findstr /S /I /C:"api_key.*=" "Source Code\*.java"
    echo Please review and move to environment variables.
) else (
    echo ✅ No hardcoded API keys found
)
echo.

REM Check for hardcoded passwords
echo [2/6] Checking for hardcoded passwords...
findstr /S /I /C:"password.*=" "Source Code\*.java" | findstr /V /C:"YOUR_PASSWORD" > nul
if %ERRORLEVEL% EQU 0 (
    echo ❌ WARNING: Potential hardcoded passwords found!
    findstr /S /I /C:"password.*=" "Source Code\*.java" | findstr /V /C:"YOUR_PASSWORD"
) else (
    echo ✅ No hardcoded passwords found
)
echo.

REM Check for TODO security items
echo [3/6] Checking for security TODOs...
findstr /S /I /C:"TODO.*security" "Source Code\*.java" > nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  Security TODOs found:
    findstr /S /I /C:"TODO.*security" "Source Code\*.java"
) else (
    echo ✅ No security TODOs found
)
echo.

REM Check environment variables
echo [4/6] Checking required environment variables...
set missing_vars=0

if "%SERPAPI_KEY%"=="" (
    echo ❌ SERPAPI_KEY not set
    set /a missing_vars+=1
)

if "%EDENAI_KEY%"=="" (
    echo ❌ EDENAI_KEY not set
    set /a missing_vars+=1
)

if "%DB_PASSWORD%"=="" (
    echo ❌ DB_PASSWORD not set
    set /a missing_vars+=1
)

if %missing_vars% EQU 0 (
    echo ✅ All required environment variables are set
) else (
    echo ❌ %missing_vars% required environment variables missing
)
echo.

REM Check for .env files in repository
echo [5/6] Checking for .env files in repository...
if exist ".env" (
    echo ❌ WARNING: .env file found in repository root!
    echo This file may contain sensitive information and should not be committed.
) else (
    echo ✅ No .env files found in repository root
)
echo.

REM Check gitignore
echo [6/6] Checking .gitignore configuration...
if exist ".gitignore" (
    findstr /C:".env" .gitignore > nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ .gitignore properly configured for .env files
    ) else (
        echo ❌ WARNING: .gitignore does not exclude .env files
    )
) else (
    echo ❌ WARNING: No .gitignore file found
)
echo.

echo ========================================
echo Security check completed
echo ========================================
echo.
echo IMPORTANT REMINDERS:
echo 1. Never commit API keys or passwords to version control
echo 2. Use environment variables for all sensitive data
echo 3. Review code changes before committing
echo 4. Keep dependencies up to date
echo 5. Use HTTPS in production
echo.
echo For more information, see SECURITY.md
echo.
pause
