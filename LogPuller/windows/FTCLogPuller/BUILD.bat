@echo off
echo ========================================
echo  FTC Log Puller -- Build Script
echo ========================================
echo.

where dotnet >nul 2>&1
if errorlevel 1 (
    echo ERROR: .NET SDK not found.
    echo Download from: https://dotnet.microsoft.com/download
    pause
    exit /b 1
)

echo Configuring NuGet source...
dotnet nuget add source https://api.nuget.org/v3/index.json --name nuget.org >nul 2>&1

echo Building FTCLogPuller.exe ...
dotnet publish -c Release -r win-x64 --self-contained true ^
    -p:PublishSingleFile=true ^
    -p:IncludeNativeLibrariesForSelfExtract=true ^
    -o .\publish

if errorlevel 1 (
    echo.
    echo Build FAILED. See errors above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  Done!  FTCLogPuller.exe is in .\publish
echo ========================================
echo.
start "" ".\publish"
pause
