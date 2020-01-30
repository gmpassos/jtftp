FROM frolvlad/alpine-java:jdk8-slim

VOLUME /app

ARG APP_NAME=jtftp
ENV APP_NAME=${APP_NAME}
ENV APP_SH="/"$APP_NAME".sh"

COPY build/distributions/*.zip /app/$APP_NAME.zip

RUN apk update \
    && apk add bash \
    && apk add openssl \
    && chmod 775 /app \
	&& unzip -o /app/$APP_NAME.zip \
	&& ln -s /*/bin/$APP_NAME /$APP_NAME.sh \
 	&& chmod a+x /$APP_NAME.sh

EXPOSE 8080

CMD $APP_SH

