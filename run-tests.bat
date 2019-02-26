@ECHO OFF
CALL rmdir /s/q "%userprofile%\.m2\repository\com\liferay\blade\com.liferay.blade.extensions.maven.profile"
CALL gradlew.bat --stop
CALL gradlew.bat --no-daemon clean
CALL gradlew.bat --no-daemon :extensions:maven-profile:publishToMavenLocal
CALL gradlew.bat --no-daemon -PmavenLocal -continue check smokeTests --scan
CALL gradlew.bat --stop