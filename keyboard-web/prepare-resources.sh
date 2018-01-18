#!/bin/sh
cd `dirname $0`
rsync -rv ../keyboard/resources/* ./src/main/webapp/resources
