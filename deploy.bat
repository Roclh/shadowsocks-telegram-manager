@echo off

chcp 65001

echo Decomposing existing docker container
docker-compose down

call mvn clean package -P silent-tests

echo Deleting previous jar
del "%CD%\src\main\docker\*.jar"

echo Moving copy from %CD%\target\pepega*.jar to %CD%\src\main\docker directory
copy "%CD%\target\pepegaVpnManager-1.0-SNAPSHOT.jar" "%CD%\src\main\docker\pepegaVpnManager-1.0-SNAPSHOT.jar" /Y

echo Changing working directory to docker
cd "src/main/docker"

echo Clearing old image of pepega-vpn-manager
docker rmi -f pepega-vpn-manager:latest

echo Building new docker image
docker build -t pepega-vpn-manager:latest .

echo Updating docker image tag
docker image tag pepega-vpn-manager:latest roclh/pepega-vpn-manager:latest

echo Pushing docker image to repo
docker image push roclh/pepega-vpn-manager:latest