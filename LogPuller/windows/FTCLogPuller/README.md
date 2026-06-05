# FTC Log Puller v2.0

A proper Windows GUI app for pulling .wpilog files from an Android device via ADB.

## Requirements to build
- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0) (or newer)

## Build
Double-click `BUILD.bat`, or run in a terminal:

    dotnet publish -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -o .\publish

The output is a single `FTCLogPuller.exe` in the `publish` folder.

## ADB
- Place `adb.exe` (and its DLLs) in an `adb\` subfolder next to the `.exe`, **or**
- The app will offer to download Platform Tools automatically on first run if connected to the internet.

## Features
- Browse to choose save folder
- Live ADB device detection with refresh button
- Coloured log output (pulled / skipped / error)
- Progress bar + stat counters
- "Delete from device after pull" toggle
- Cancel mid-pull
- Opens destination folder when done
- Single self-contained .exe, no installer
