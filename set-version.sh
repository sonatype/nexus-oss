#!/bin/bash
#
# Sonatype Nexus (TM) Open Source Version
# Copyright (c) 2008-present Sonatype, Inc.
# All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
#
# This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
# which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
#
# Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
# of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
# Eclipse Foundation. All other trademarks are the property of their respective owners.
#


# This will update all pom.xml files to use version specified as first parameter of this script

newVersion=$1
if [ -z "$newVersion" ]; then
    echo "usage: `basename $0` <new-version>"
    exit 1
fi

mvn org.eclipse.tycho:tycho-versions-plugin:0.18.0:set-version \
    -Dtycho.mode=maven \
    -Dartifacts=nexus-oss \
    -Dproperties=nexus.version \
    -DnewVersion="$newVersion"
