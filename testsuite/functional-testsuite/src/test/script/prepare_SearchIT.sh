#!/usr/bin/env bash

#need to insert the raw repo manually from the karaf console with the 2 following commands:
#    orient:connect plocal:data/db/config admin admin
#    orient:insert 'into repository_configuration SET repository_name="search-test-raw", recipe_name="raw-hosted", online=true, attributes={"storage":{"blobStoreName":"default","writePolicy":"ALLOW"}}'

curl -u admin:admin123 -X PUT -v -include -F package=@../../../../../testsupport/nexus-nuget-test-data/src/main/resources/nuget/SONATYPE.TEST.1.0.nupkg http://localhost:8081/repository/nuget-hosted/
curl -u admin:admin123 -X PUT -v -include -F package=@../../../../../testsupport/nexus-maven-test-data/src/main/resources/maven/aopalliance-1.0.jar http://localhost:8081/repository/maven-releases/aopalliance/aopalliance/1.0/aopalliance-1.0.jar
curl -u admin:admin123 -H 'Content-Type: text/plain' --upload-file ../../../../../testsupport/nexus-raw-test-data/src/main/resources/raw/alphabet.txt http://localhost:8081/repository/search-test-raw/alphabet.txt
                      
