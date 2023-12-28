#!/bin/bash
### run simple web server to test app
pushd "`dirname $0`/build/webapp" ; python -m http.server ; popd
