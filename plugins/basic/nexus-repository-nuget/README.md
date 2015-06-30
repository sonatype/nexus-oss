<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2008-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
# Configure Repositories

    orient:connect plocal:../sonatype-work/nexus/db/config admin admin
    orient:insert 'into repository_configuration SET repository_name="nugethosted1", recipe_name="nuget-hosted"'
    orient:insert 'into repository_configuration SET repository_name="nugetproxy", recipe_name="nuget-proxy", attributes={"proxy": { "remoteUrl": "http://www.nuget.org/api/v2/", "artifactMaxAge" : 5 }, "httpclient":{"connection":{"timeout":20000, "retries":2}}}'
    orient:insert 'into repository_configuration SET repository_name="nugetgroup", recipe_name="nuget-group", attributes={"group": { "memberNames": ["nugethosted1", "nugetproxy"] }}'
    system:shutdown --force --reboot

# Interact

Note: curl commands have been escaped for bash

## Hosted

    curl -u admin:admin123 -X PUT -v -include -F package=@src/test/resources/SONATYPE.TEST.1.0.nupkg http://localhost:8081/repository/nuget-hosted/
    curl -u admin:admin123 -X GET -v http://localhost:8081/repository/nuget-hosted/SONATYPE.TEST/1.0 -o SONATYPE.TEST.1.0.nupkg
    curl -u admin:admin123 -X DELETE -v http://localhost:8081/repository/nuget-hosted/SONATYPE.TEST/1.0

### Viewing Components in OrientDB

    orient:connect plocal:../sonatype-work/nexus/db/component admin admin
    orient:select * from component
    orient:exportrecord json

# Typical Visual Studio Queries

## Default Search when VS is opened

    curl -u admin:admin123 -X GET -v "http://localhost:8081/repository/nuget.org-proxy/Search()/\$count?\$filter=IsLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=false"
    curl -u admin:admin123 -X GET -v "http://localhost:8081/repository/nuget.org-proxy/Search()?\$filter=IsLatestVersion&\$orderby=DownloadCount%20desc,Id&\$skip=0&\$top=30&searchTerm=''&targetFramework='net45'&includePrerelease=false"

## Searching for 'Web'

    curl -u admin:admin123 -X GET -v "http://localhost:8081/repository/nuget.org-proxy/Search()/\$count?\$filter=IsLatestVersion&searchTerm='Web'&targetFramework='net45'&includePrerelease=false"
    curl -u admin:admin123 -X GET -v "http://localhost:8081/repository/nuget.org-proxy/Search()?\$filter=IsLatestVersion&\$skip=0&\$top=30&searchTerm='Web'&targetFramework='net45'&includePrerelease=false"
