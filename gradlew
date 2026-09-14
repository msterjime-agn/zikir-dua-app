#!/bin/sh
APP_HOME=$(cd "${0%/*}" && pwd)
exec gradle "$@"
