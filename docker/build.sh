#!/bin/bash
set -e

# Edit these properties when moving to a new version.
VERSION=3.2.0.201810082311
# SHA-256...
SHA=47a38378b93ca0c6e7751842fb70111bbffb750be9e7a3ebe42b7906f8d9b6ff

mkdir -p tmp
if [ ! -f tmp/blade.jar ]; then
	echo Downloading blade JAR version $VERSION
	curl https://releases.liferay.com/tools/blade-cli/3.2.0.201810082311/blade.jar > tmp/blade.jar
else
	echo Already downloaded blade JAR version $VERSION. If SHA check fails, try deleting tmp folder.
fi

echo Checking SHA...
echo "$SHA  tmp/blade.jar" > tmp/checksum
shasum -a 256 -c tmp/checksum

echo Building Docker image
docker build --quiet -t gamerson/blade:$VERSION -t gamerson/blade:latest .

echo DONE! Try the following:
echo alias bladed=\''docker run -it -v "$HOME":"$HOME" -v "$(pwd)":/data' gamerson/blade:latest\'
echo bladed version