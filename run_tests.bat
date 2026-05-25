@echo off
setlocal

rem Set Java 21 home
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

rem Find JUnit and Mockito jars
set JUNIT_JAR=%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.junit.jupiter\junit-jupiter\5.10.0\*
set MOCKITO_CORE=%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.mockito\mockito-core\5.5.0\*
set MOCKITO_JUNIT=%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.mockito\mockito-junit-jupiter\5.5.0\*

rem Find the compiled test classes
set TEST_CLASSES=build\classes\java\test

rem Create classpath
set CLASSPATH=%TEST_CLASSES%

rem Add JUnit jars
for %%f in ("%JUNIT_JAR%") do set CLASSPATH=%CLASSPATH%;%%f
for %%f in ("%MOCKITO_CORE%") do set CLASSPATH=%CLASSPATH%;%%f
for %%f in ("%MOCKITO_JUNIT%") do set CLASSPATH=%CLASSPATH%;%%f

rem Add main classes
set CLASSPATH=%CLASSPATH%;build\classes\java\main

rem Add Fabric API and other dependencies from gradle cache
set FABRIC_API=%USERPROFILE%\.gradle\caches\modules-2\files-2.1\net.fabricmc.fabric-api\fabric-api\*
for %%f in ("%FABRIC_API%") do set CLASSPATH=%CLASSPATH%;%%f

rem Add Archipelago client
set ARCHIPELAGO=%USERPROFILE%\.gradle\caches\modules-2\files-2.1\io.github.archipelagomw\Java-Client\0.2.1\*
for %%f in ("%ARCHIPELAGO%") do set CLASSPATH=%CLASSPATH%;%%f

echo Running tests with classpath:
echo %CLASSPATH%
echo.

rem Run JUnit Platform Console Launcher
java -cp "%CLASSPATH%" org.junit.platform.console.ConsoleLauncher --select-class com.minecraftarchipelago.SimpleTest --details verbose
endlocal