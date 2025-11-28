@ECHO OFF
@REM Copyright 2015 the original author or authors.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.

@IF "%DEBUG%"=="" @ECHO OFF
SETLOCAL

SET DIRNAME=%~dp0
IF "%DIRNAME%"=="" SET DIRNAME=.
SET APP_BASE_NAME=%~n0
SET APP_HOME=%DIRNAME%

@REM Resolve any . or .. segments in APP_HOME to make it shorter.
FOR %%i IN ("%APP_HOME%") DO SET APP_HOME=%%~fi

@REM Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

SET JAVA_EXE=java.exe
IF DEFINED JAVA_HOME (
    SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

IF NOT EXIST "%JAVA_EXE%" (
    ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    EXIT /B 1
)

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
