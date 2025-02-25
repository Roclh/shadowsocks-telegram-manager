@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

:: Инициализация переменных ошибок
set "STEP_NAME=Initialization"
set "ERROR_MSG=Unknown error"

:: Главная процедура
call :main
exit /b %ERRORLEVEL%

:main
    :: Шаг 1: Остановка контейнеров
    set "STEP_NAME=Docker compose down"
    echo [STEP] Decomposing existing docker container
    docker-compose -f "%CD%\src\main\docker\docker-compose.yml" down

    :: Шаг 2: Сборка проекта Maven
    set "STEP_NAME=Maven build"
    echo [STEP] Building project with Maven
    call mvn clean package -P silent-tests
    if %ERRORLEVEL% neq 0 (
        set "ERROR_MSG=Maven build failed"
        goto error
    )

    :: Шаг 3: Удаление старого JAR-файла
    set "STEP_NAME=Delete old JAR"
    echo [STEP] Deleting previous jar
    if exist "%CD%\src\main\docker\*.jar" (
        del "%CD%\src\main\docker\*.jar" 2> nul
        if %ERRORLEVEL% neq 0 (
            set "ERROR_MSG=Failed to delete JAR files"
            goto error
        )
    )

    :: Шаг 4: Копирование нового JAR-файла
    set "STEP_NAME=Copy new JAR"
    echo [STEP] Copying new JAR file
    if not exist "%CD%\target\pepegaVpnManager-1.0-SNAPSHOT.jar" (
        set "ERROR_MSG=JAR file not found in target directory"
        goto error
    )
    copy "%CD%\target\pepegaVpnManager-1.0-SNAPSHOT.jar" "%CD%\src\main\docker\" /Y > nul
    if %ERRORLEVEL% neq 0 (
        set "ERROR_MSG=Failed to copy JAR file"
        goto error
    )

    :: Шаг 5: Переход в директорию docker
    set "STEP_NAME=Change directory"
    echo [STEP] Changing working directory
    cd "src/main/docker" 2> nul
    if %ERRORLEVEL% neq 0 (
        set "ERROR_MSG=Directory not found: src/main/docker"
        goto error
    )

    :: Шаг 6: Удаление старого образа Docker
    set "STEP_NAME=Remove old image"
    echo [STEP] Removing old Docker image
    docker rmi -f pepega-vpn-manager:latest 2> nul
    if %ERRORLEVEL% neq 0 (
        echo [WARNING] Could not remove old image (might not exist)
    )

    :: Шаг 7: Запуск контейнеров
    set "STEP_NAME=Docker compose up"
    echo [STEP] Starting containers
    docker-compose up
    if %ERRORLEVEL% neq 0 (
        set "ERROR_MSG=Failed to start containers"
        goto error
    )

    :: Успешное завершение
    echo.
    echo [SUCCESS] Build and deployment completed
    exit /b 0

:error
    echo.
    echo [ERROR] %ERROR_MSG%
    echo [FAILED STEP] %STEP_NAME%
    echo.
    exit /b 1