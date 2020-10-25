#!/bin/sh
cd `dirname $0`
rsync -rv ../analyzer/resources/* ./src/main/webapp/resources
