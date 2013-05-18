<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2007-2013 Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->

# Sonatype Nexus Core

This is the Core codebase of Sonatype Nexus, aka "Nexus OSS".

## Quick Links

* [Product homepage](http://www.sonatype.org/nexus/participate)
* [Public source repository](https://github.com/sonatype/nexus)
* [Issue tracking](https://issues.sonatype.org/browse/NEXUS)
* [Public wiki](https://docs.sonatype.com/display/SPRTNXOSS)

## Building

To build this project you need recent version of Apache Maven (3.0.4+) and Sonatype Forge set up as mirror at least.
Example Maven settings XML:

```
<?xml version="1.0" encoding="UTF-8"?>
<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2007-2013 Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <!-- This sends everything to Forge -->
      <id>sonatype-forge</id>
      <mirrorOf>external:*</mirrorOf>
      <url>https://repository.sonatype.org/content/groups/sonatype-public-grid/</url>
    </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>nexus</id>
      <!-- Enable snapshots for the built in central repo to direct -->
      <!-- all requests to nexus via the mirror -->
      <repositories>
        <repository>
          <id>central</id>
          <url>http://central</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
     <pluginRepositories>
        <pluginRepository>
          <id>central</id>
          <url>http://central</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <activeProfiles>
    <!-- make the profile active all the time -->
    <activeProfile>nexus</activeProfile>
  </activeProfiles>
  <pluginGroups>
    <!-- define the sonatype plugin group, so the nexus plugins will work without typing the groupId -->
    <pluginGroup>org.sonatype.plugins</pluginGroup>
  </pluginGroups>
</settings>
```

Note: As Maven "best practice", it is highly recommended to set up your own instance of Nexus and your local builds should use it instead of directly 
reaching out, fetch from remote. In such case, to properly proxy the forge URL above, be sure to add both release and snapshot proxy repositories for it, as
it is actually a repository group having "mixed" repository policy.

Have fun,  
Sonatype Team
