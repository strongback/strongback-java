@echo off

if "%1"=="" goto help

set strongback_home=%~dp0\..
set cmd=%1
shift

GOTO %cmd%
:new-project
    java -cp %strongback_home%\libs\strongback-tools.jar org.strongback.tools.newproject.NewProject %*
    GOTO END
:log-decoder
    java -cp %strongback_home%\libs\strongback-tools.jar org.strongback.tools.logdecoder.LogDecoder %*
    GOTO END
:help
    echo usage: strongback ^<command^>
    echo.
    echo Commands
    echo     new-project
    echo         Creates a new project configured to use strongback
    echo.
    echo     log-decoder
    echo         Converts a Strongback Binary Log to a readable CSV
    GOTO END
:END
