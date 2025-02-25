# Dockerfile-postgres
FROM amazoncorretto:18.0.2-alpine3.16
WORKDIR /app
ARG JAR_FILE=*.jar
COPY pepegaVpnManager-1.0-SNAPSHOT.jar /app/pepegaVpnManager.jar
RUN apk upgrade --update \
    && apk add bash screen  && \
    set -ex && \
    echo 'http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories && \
    echo 'http://dl-cdn.alpinelinux.org/alpine/edge/main' >> /etc/apk/repositories && \
    apk --update add --no-cache \
    curl \
    privoxy \
    libsodium \
    mbedtls \
    udns \
    pcre \
    iproute2 \
    libev \
    shadowsocks-libev && \
    mkdir -p /etc/shadowsocks-libev

# Скачиваем архив
RUN wget -q https://github.com/shadowsocks/v2ray-plugin/releases/download/v1.3.2/v2ray-plugin-linux-amd64-v1.3.2.tar.gz

# Распаковываем архив в текущую директорию (с поддиректорией)
RUN tar -xf v2ray-plugin-linux-amd64-v1.3.2.tar.gz

# Перемещаем файл из поддиректории в /usr/bin/v2ray-plugin
RUN cp v2ray-plugin_linux_amd64 /usr/bin/v2ray-plugin

# Даем права на выполнение
RUN chmod +x /usr/bin/v2ray-plugin

# Удаляем ненужные файлы
RUN rm -rf v2ray-plugin-linux-amd64-v1.3.2.tar.gz

EXPOSE 8800
CMD java -jar pepegaVpnManager.jar