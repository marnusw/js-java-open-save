@echo off
echo.
echo --------------------------------------------
echo This script must be run as an administrator!
echo --------------------------------------------
echo.
echo Right click the script and select "Run as administrator".
echo.
echo When adding multiple IP addresses use a cmd window with command history.
echo Open Explorer and navigate to C:\Windows\System32. Right click cmd.exe
echo and select "Run as administrator". Then run this script again.
echo.
echo --------------------------------------------
echo.

set /p JRE_VER=JRE version number (see C:/Program Files/Java/jreX) [Enter for 7]: 
set /p IP_ADDR=The IP address or domain you're accessing: 
echo.

IF NOT "%JRE_VER%" == "" GOTO :VALUE_SET
  set JRE_VER=7
:VALUE_SET

set POLICY_FILE="C:\Program Files (x86)\Java\jre%JRE_VER%\lib\security\java.policy"

echo. >> %POLICY_FILE%
echo grant codeBase "http://%IP_ADDR%/lib/js-java-open-save/JsJavaOpenSave.jar" { >> %POLICY_FILE%
echo     permission java.security.AllPermission; >> %POLICY_FILE%
echo }; >> %POLICY_FILE%

echo --------------------------------------------
echo.
echo Updated %POLICY_FILE%
echo.

set /p dummy=Press Enter to continue...
