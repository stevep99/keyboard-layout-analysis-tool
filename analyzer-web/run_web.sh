#!/bin/bash
### run simple web server to test app
pushd "`dirname $0`/target/keyboardTeaVM-1.0-SNAPSHOT" ; python -m SimpleHTTPServer ; popd
