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
# Configure Repositories via UI

Use this config to create Maven2 hosted repository, use name `maven2-hosted`:

Hosted "maven2-hosted":
```
{
  "maven" : {
    "versionPolicy" : "SNAPSHOT",
    "checksumPolicy" : "WARN",
    "strictContentTypeValidation" : false
  }
}
```

Proxy "maven2-proxy":
```
{
  "maven" : {
    "versionPolicy" : "RELEASE",
    "checksumPolicy" : "WARN",
    "strictContentTypeValidation" : false
  },
  "proxy" : {
    "remoteUrl" : "http://repo1.maven.org/maven2/",
    "artifactMaxAge" : 3600
  },
  "httpclient" : {
    "connection" : {
      "timeout" : 1500,
      "retries" : 3
    },
    "authentication" : {
        "username" : "admin",
        "password" : "admin123"
    }
  }
}
```

Group "maven2-group":
```
{
  "group" : {
    "memberNames" : ["maven2-hosted", "maven2-proxy"]
  }
}

```

## Building Maven master branch and deploying it to NX3 itself

* create "maven2-hosted" repository with snippet above (to host mavan snapshot deploy)
* create "maven2-proxy" repository with snippet above to proxy Central
* create "maven2-group" repository with snippet above to group the two
* check out Apache Maven (or you can use any other prj that will have satisfied dependencies): https://github.com/apache/maven
* prepare environment, by putting this settings.xml below in project root directory (note: license header is a MUST due to stupid RAT plugin used by all ASF builds. Same thing for localRepo location, must be OUTSIDE of checkout, otherwise RAT chokes on it)
```
<?xml version="1.0" encoding="UTF-8"?>

  <!--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements. See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to you under the Apache License, Version
    2.0 (the "License"); you may not use this file except in compliance
    with the License. You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0 Unless required by
    applicable law or agreed to in writing, software distributed under
    the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
    OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and
    limitations under the License.
  -->

<settings>

    <localRepository>../maven-m2-repository</localRepository>

	<servers>
		<server>
			<id>local-nexus-admin</id>
			<username>admin</username>
			<password>admin123</password>
		</server>
	</servers>
	<mirrors>
		<mirror>
			<id>local-nexus-admin</id>
			<mirrorOf>external:*</mirrorOf>
			<url>http://localhost:8081/repository/maven2-group/</url>
		</mirror>
	</mirrors>
</settings>
```
* kick of a build and deploy:
```
mvn -s settings.xml clean deploy -Dtest=void -DfailIfNoTests=false -DaltDeploymentRepository=local-nexus-admin::default::http://localhost:8081/repository/maven2-hosted/ -U
```
Note: the `-U` was needed in my case as currently NX Proxy HttpClient "chokes" more often that should, and autoblocks Central proxy

## As it looks

http://monosnap.com/file/90DMXbL5M102QyCbkGE31VqcT7Sz1u


