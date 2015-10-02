#!/bin/bash
strongback_home=$( cd "$(dirname "${BASH_SOURCE}")/.." ; pwd -P )

cd "$strongback_home"
java -cp libs/strongback-tools.jar org.strongback.tools.newproject.NewProject $@
