<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2008-2015 Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
# Configure Repositories

    orient:connect plocal:../sonatype-work/nexus/db/config admin admin
    orient:insert 'into repository_configuration SET repository_name="simplehosted1", recipe_name="simple-hosted"'
    orient:insert 'into repository_configuration SET repository_name="simplehosted2", recipe_name="simple-hosted"'
    orient:insert 'into repository_configuration SET repository_name="simpleproxy1", recipe_name="simple-proxy", attributes={"proxy": { "remoteUrl": "http://repo1.maven.org/maven2/junit/junit" }}'
    orient:insert 'into repository_configuration SET repository_name="simplegroup1", recipe_name="simple-group", attributes={"group": { "memberNames": ["simplehosted1", "simplehosted2", "simpleproxy1"] }}'
    system:shutdown --force --reboot

# Interact

## Hosted

    curl -v -H 'Content-Type: text/plain' --upload-file ./README.md http://localhost:8081/repository/simplehosted1/README.md
    curl -v --upload-file ./README.md http://localhost:8081/repository/simplehosted1/no-type-README.md
    curl -v -X GET http://localhost:8081/repository/simplehosted1/
    curl -v -X GET http://localhost:8081/repository/simplehosted1/index.html
    curl -v -X GET http://localhost:8081/repository/simplehosted1/README.md
    curl -v -X DELETE http://localhost:8081/repository/simplehosted1/README.md
    curl -v -X DELETE http://localhost:8081/repository/simplehosted1/no-type-README.md

## Group

    curl -v -H 'Content-Type: text/plain' --upload-file ./README.md http://localhost:8081/repository/simplehosted1/A.md
    curl -v -H 'Content-Type: text/plain' --upload-file ./README.md http://localhost:8081/repository/simplehosted2/B.md
    curl -v -X GET http://localhost:8081/repository/simplegroup1/

## Proxy

    curl -v -X GET http://localhost:8081/repository/simpleproxy1/4.12/junit-4.12.pom
    curl -v -X GET http://localhost:8081/repository/simpleproxy1/maven-metadata.xml
