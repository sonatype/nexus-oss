#!/bin/bash

# This will update all pom.xml files to use version specified as first parameter of this script

mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:0.18.0-SNAPSHOT:set-version \
  -Dartifacts=nexus-plugin-parent,nexus,nexus-runtime-platform -Dproperties=nexus.version \
  -DnewVersion=$1
