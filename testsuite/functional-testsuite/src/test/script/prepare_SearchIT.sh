#!/usr/bin/env bash
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


#need to insert the raw repo manually from the karaf console with the 2 following commands:
#    orient:connect plocal:data/db/config admin admin
#    orient:insert 'into repository_configuration SET repository_name="search-test-raw", recipe_name="raw-hosted", online=true, attributes={"storage":{"blobStoreName":"default","writePolicy":"ALLOW"}}'

curl -u admin:admin123 -X PUT -v -include -F package=@../../../../../testsupport/nexus-nuget-test-data/src/main/resources/nuget/SONATYPE.TEST.1.0.nupkg http://localhost:8081/repository/nuget-hosted/
curl -u admin:admin123 -X PUT -v -include -F package=@../../../../../testsupport/nexus-maven-test-data/src/main/resources/maven/aopalliance-1.0.jar http://localhost:8081/repository/maven-releases/aopalliance/aopalliance/1.0/aopalliance-1.0.jar
curl -u admin:admin123 -H 'Content-Type: text/plain' --upload-file ../../../../../testsupport/nexus-raw-test-data/src/main/resources/raw/alphabet.txt http://localhost:8081/repository/search-test-raw/alphabet.txt
                      
