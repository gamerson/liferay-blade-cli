FROM java:8-jre-alpine
MAINTAINER Gregory Amerson <gregory.amerson@liferay.com>

ADD tmp/blade.jar /

ENTRYPOINT ["java", "-jar", "/blade.jar"]

WORKDIR /data
