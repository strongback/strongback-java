#!/bin/bash

strongback_home=$( cd "$(dirname "${BASH_SOURCE}")/../.." ; pwd -P )
cmd=$1
shift

case $cmd in
    "new-project")
        java -cp $strongback_home/java/lib-tools/strongback-tools.jar org.strongback.tools.newproject.NewProject "$@"
        ;;
    "log-decoder")
        java -cp $strongback_home/java/lib-tools/strongback-tools.jar org.strongback.tools.logdecoder.LogDecoder "$@"
        ;;
    "version")
        grep version $strongback_home/strongback.properties | sed -n -e 's/^.*=//p'
        ;;
    *)
        echo "usage: strongback <command>"
        echo
        echo "Commands"
        echo "    new-project"
        echo "        Creates a new project configured to use strongback"
        echo
        echo "    log-decoder"
        echo "        Converts a Strongback Binary Log to a readable CSV"
        echo
        echo "    version"
        echo "        Prints the version of Strongback that is installed"
        ;;
esac
    
