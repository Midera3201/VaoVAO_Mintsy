@echo off
setlocal

set APP_NAME=test-app
set TOMCAT_HOME=C:\apache-tomcat-10.1.55
set TOMCAT_WEBAPPS=%TOMCAT_HOME%\webapps

pushd "%~dp0.."

echo Installation du framework dans Maven local...
call mvn -q -f framework\pom.xml clean install
if errorlevel 1 goto error

echo Construction de l'application de test...
call mvn -q -f test\pom.xml clean package
if errorlevel 1 goto error

echo.
echo Verification du contenu du war (controllers presents ?)...
jar tf "test\target\%APP_NAME%.war" | findstr /I "controller"
echo.

if not exist "%TOMCAT_WEBAPPS%" (
    echo Dossier Tomcat introuvable: %TOMCAT_WEBAPPS%
    echo Modifie TOMCAT_HOME dans test\deploy.bat si ton Tomcat est ailleurs.
    goto error
)

echo Suppression de l'ancien deploiement (evite les caches Tomcat)...
if exist "%TOMCAT_WEBAPPS%\%APP_NAME%.war" del /F /Q "%TOMCAT_WEBAPPS%\%APP_NAME%.war"
if exist "%TOMCAT_WEBAPPS%\%APP_NAME%" rmdir /S /Q "%TOMCAT_WEBAPPS%\%APP_NAME%"

echo Deploiement vers Tomcat...
copy /Y "test\target\%APP_NAME%.war" "%TOMCAT_WEBAPPS%\%APP_NAME%.war"
if errorlevel 1 goto error

echo.
echo Deploiement termine.
echo URL: http://localhost:8080/%APP_NAME%/
echo Exemple: http://localhost:8080/%APP_NAME%/aaa
echo.
echo Si la liste des controllers est vide dans le navigateur, regarde
echo %TOMCAT_HOME%\logs\catalina.out (ou catalina.YYYY-MM-DD.log) :
echo le servlet logue le package scanne et le nombre de classes trouvees au demarrage.

popd
endlocal
exit /b 0

:error
popd
endlocal
exit /b 1
