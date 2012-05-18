#!/bin/bash

FULL_REV=$(git rev-parse HEAD)
SHORT_REV=${FULL_REV:0:8}

scp bin/smartrek-android.apk static.suminb.com:webapps/static/smartrek/smartrek-${SHORT_REV}.apk
echo "http://static.suminb.com/smartrek/?rev=${SHORT_REV}"
