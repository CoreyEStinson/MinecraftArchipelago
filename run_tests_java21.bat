@echo off
setlocal

echo Setting up Java 21 environment...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Verifying Java version:
java -version
echo.

echo Running Gradle with Java 21...
cd /d A:\MinecraftArchipelago
call gradlew compileTestJava --no-daemon

if %ERRORLEVEL% equ 0 (
    echo.
    echo Test compilation successful!
    echo.
    echo You can now run tests in IntelliJ IDEA:
    echo 1. Open the project in IntelliJ
    echo 2. Go to Run -> Edit Configurations
    echo 3. Add a new JUnit configuration
    echo 4. Select the test class you want to run
    echo 5. Click Run
) else (
    echo.
    echo Test compilation failed!
)

endlocal
pause