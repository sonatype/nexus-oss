<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2007-2012 Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
# Nexus Yum Plugin

A plugin for Sonatype Nexus 1.9.2.x or 2.X which recognizes RPMs in Nexus Maven repositories and generates 
Yum metadata, so that RedHat-compatible system can use Nexus as software repository. 

## Content

1. [Audience](#audience)
1. [History](#history)
1. [Features](#features)
1. [Requirements](#requirements)
1. [Installation](#installation)
1. [Configuration](#configuration)
1. [Getting Started](#getting-started)
	1. [Deploy Java Web Application via RPM](#deploy-java-web-application-via-rpm)
	1. [Staging RPMs in Nexus Professional](#staging-rpms-in-nexus-professional)
	1. [Staging RPMs in Nexus OSS](#staging-rpms-in-nexus-oss)
1. [Help & Issues](#help--issues)
1. [How to Build](#how-to-build)


## Audience

The Nexus Yum Plugin is for anyone who is deploying Java applications to RedHat-compatible (RHEL, Centos, Fedora) servers via RPM.

### Default use case

![CLD at IS24][1]
See [Deploy Java Web Application via RPM](#deploy-java-web-application-via-rpm).

## History

[Sonatype Nexus][2] is a common repository manager for [Maven][3], used by many companies to manage 
their Java artifacts like JAR, WAR, pom.xml files. At [ImmobilienScout24][4] the DevOps-guys started 
[to deploy their configurations][5] and 
[java applications via RPMs][6] and 
wanted to have a repository manager for their application RPMs. Why don't extend Sonatype Nexus to host RPMs as well?

## Features

- Use a Maven repository, hosted in Nexus, containing RPMs as if it is a Yum repository. This leverages the virtual repository mechanism in Nexus which allows you to use Maven tooling to deploy RPMs into a Maven repository but still allow Yum clients to interact with the repository using the protocol it understands.
- Yum repositories are automatically updated if you upload/deploy/delete a new RPM into Nexus.
- Full group support so that you can logically group a set of Yum repositories behind a single URL.
- Have versioned views on repositories: <pre>http://your.nexus/nexus/service/local/yum/repos/releases/1.2.3/</pre> gives you a Yum repository with all packages in version *1.2.3* in repository releases.
- You can define aliases for specific versions eg. *production=1.2* and *testing=2.0* and access them via the alias: <pre>http://your.nexus/nexus/service/local/yum/repos/releases/testing/</pre> and <pre>http://your.nexus/nexus/service/local/yum/repos/releases/production/</pre> to get constant repository URLs for your servers. A new release is then applied to the server via setting the alias to a new version.
- Create Yum createrepo tasks manually via web interface.
- Multiple createrepo tasks on the same repository get merged.
- Use Yum group repositories as target of staging repositories (Nexus Pro)


## Requirements

The Nexus Yum Plugin uses the command line "createrepo" utility to build yum repository metadata.  This utility must be on the path of the system that is running Nexus.

If your Centos/Redhat/Fedora system does not have this command you can install it by running "yum install createrepo" as root.

## Installation

From Nexus version 2.3 on the plugin is bundled together with Nexus OSS and Nexus Pro.

## Configuration

Since Nexus 2.3 you can configure Yum metadata capabilities for dedicated repositories. Click on
![Configure-Capabilities][16]
to open the capabilities configuration tab.

### Configure Hosted Yum Repositories

Next step is creating a new *Yum: Generate Metadata* capability for one of your hosted repositories.
Click *Add* and create one for the 3rd party repository for example:
![Configure-3rd-Party-Repo][17]
As you see we also configured some optional aliases to provide separate versioned repository for different stages (see [Staging RPMs in Nexus OSS](#staging-rpms-in-nexus-oss)).
After saving the settings each uploaded RPM to the 3rd party repository (see [Getting Started](#getting-started)) causes Nexus to rebuild the yum metadata immediately.

### Configure Group Repositories

Similar to single repositories Nexus group repositories can be configured to merge the yum metadata
of their member repositories. Click *Add* and create a new *Yum: Merge Metadata* capability for one group repository:
![Config-GroupRepository][18]

### Location

The configuration of the Nexus Yum Plugin can be found in *yum.xml* in the same directory as *nexus.xml* :

	$NEXUS_WORK_DIR/conf/yum.xml

default:

	~/sonatype-work/nexus/conf/yum.xml

but shouldn't be edited directly.

## Getting Started

Here we provide some typical scenarios in which the _nexus-yum-plugin_ is used.

### Deploy Java Web Application via RPM

#### Prepare the _pom.xml_

Assume you have a standard Java web application build with [Maven][3]. To build a RPM of your WAR file you could
use the [rpm-maven-plugin][12] by Codehaus. Its goal _attached-rpm_ automatically attaches the RPM file as Maven 
build artifact so that the RPM is uploaded to Nexus in the _deploy_ phase. A minimal _pom.xml_ would look like this:
```xml
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
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.sonatype.nexus.yum.its</groupId>
  <artifactId>war-rpm-test</artifactId>
  <version>1.0</version>
  <packaging>war</packaging>
  <build>
    <plugins>
      <!-- use rpm-maven-plugin to package the war into an rpm -->
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>rpm-maven-plugin</artifactId>
        <version>2.1-alpha-2</version>
        <executions>
          <execution>
            <id>build-rpm</id>
            <goals>
           	  <!-- this goal automatically adds the rpm as Maven build artifact -->
              <goal>attached-rpm</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <group>Applications/Internet</group>
          <copyright>EPL</copyright>
          <!-- require tomcat6 as webapp container -->
          <requires>
            <require>tomcat6</require>
          </requires>
          <mappings>
            <mapping>
              <!-- put webapp files to standard tomcat6 webapps directory -->
              <directory>/var/lib/tomcat6/webapps/${project.build.finalName}</directory>
              <sources>
                <source>
                  <location>${project.build.directory}/${project.build.finalName}</location>
                </source>
              </sources>
            </mapping>
          </mappings>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <!-- deploy build artifacts (pom,war,rpm) to Nexus --> 
  <distributionManagement>
    <repository>
      <id>releases</id>
      <name>Releases Repository</name>
      <url>http://your.nexus.domain/nexus/content/repositories/releases</url>
    </repository>
  </distributionManagement>
</project>
```
	
#### Deploy RPM to Nexus
	
If you have the _nexus-yum-plugin_ [installed](#installation) and deploy your application via

	mvn deploy
	
to Nexus, the RPM is uploaded and on Nexus side the _yum_ metatdata is generated asynchronously. You can 
browse the _yum_ metadata here:

	http://your.nexus.domain/nexus/content/repositories/releases/repodata

#### Install RPM on Server

Your RPM was built and is stored in a Nexus _yum_ repository (if _nexus_yum_plugin_ is installed, each Maven 
repository gets a _yum_ repository after uploading an RPM). The next step is to install the RPM on your RHEL-compatible server.

First of all, we need to introduce our new _yum_ repository to the server. Therefore, we create a new _yum repository file_ called
_nexus-releases.repo_. The default location for such _yum repository file_ is _/etc/yum.repos.d_, but may differ depending on 
your distribution and configuration.:

	sudo vi /etc/yum.repos.d/nexus-releases.repo
	
Insert the following content:

	[nexus-releases]
	name=Nexus Releases Repository
	baseurl=http://your.nexus.domain/nexus/content/repositories/releases
	enabled=1
	protect=0
	gpgcheck=0
	metadata_expire=30s
	autorefresh=1
	type=rpm-md 
	
and save the file. Now, the server will ask Nexus for new software packages and updates. After that just install your web application 
and all its dependencies (Tomcat, Java, etc.) via:

	sudo yum install war-rpm-test
	
and start tomcat:

	sudo service tomcat start
	
That's it.

#### Update RPM

To update the web application on your server just call:

	sudo service tomcat stop
	sudo yum update
	sudo service tomcat start
	
The tomcat restart is optional depending on your webapp and configuration, but always a good choice.

#### Summary

The _nexus-yum-plugin_ makes deploying Java application to _real_ RHEL-compatible servers really easy and works as a 
relyable platform for your deployments.  

### Staging RPMs in Nexus Professional

The [Staging Suite][13] in _Nexus Professional_ improves a stage-based release process a lot. The _Nexus Yum Plugin_ supports
this staging configuration, but requires some fine tuning to use the full feature list.

Image you want to configure a one-stage release process as described [in the Nexus book][14]. Follow the tutorial but use 
_Maven2Yum_ as _Template_ for target repositories, staging profiles and group repositories. The advantage is that _Maven2Yum_ 
group repository really merges their member yum repositories by using [_mergerepo_][15]. This allows you to use these group 
repositories with their constant url for _\*.repo_ files on the server.

### Staging RPMs in Nexus OSS 

The open source verion of _Nexus_ doesn't contain such a nice [Staging Suite][13], but you can stage your RPMs 
(without the jar, war, etc. files) anyway. 

Image you have 3 stages for your RPMs:

1. _development_
1. _test & verification_
1. _production_

and already a RPM package in version 1.0 in your repository called _releases_.

#### Create aliases

First of all you create version aliases for each stage. These aliases allow you to have a canonical repository url for each stage. 
You can use _curl_ for the initial creation:

	curl -d "1.0" --header "Content-Type: text/plain" http://your.nexus.domain/nexus/service/local/yum/alias/releases/development/
	curl -d "1.0" --header "Content-Type: text/plain" http://your.nexus.domain/nexus/service/local/yum/alias/releases/verification/
	curl -d "1.0" --header "Content-Type: text/plain" http://your.nexus.domain/nexus/service/local/yum/alias/releases/production/   

#### Prepare servers

Now, you are able to add these alias repositories to your server.
On your _development_ machine create a file named _/etc/yum.repos.d/nexus-dev.repo_ and the following content:

	[nexus-dev]
	name=Nexus Dev Repository
	baseurl=http://your.nexus.domain/nexus/service/local/yum/repos/releases/development/
	enabled=1
	protect=0
	gpgcheck=0
	metadata_expire=30s
	autorefresh=1
	type=rpm-md
	
On your _verification_ machine create a file named _/etc/yum.repos.d/nexus-verification.repo_ and the following content:

	[nexus-verification]
	name=Nexus Verification Repository
	baseurl=http://your.nexus.domain/nexus/service/local/yum/repos/releases/verification/
	enabled=1
	protect=0
	gpgcheck=0
	metadata_expire=30s
	autorefresh=1
	type=rpm-md
	
On your _production_ machine create a file named _/etc/yum.repos.d/nexus-production.repo_ and the following content:

	[nexus-production]
	name=Nexus Production Repository
	baseurl=http://your.nexus.domain/nexus/service/local/yum/repos/releases/production/
	enabled=1
	protect=0
	gpgcheck=0
	metadata_expire=30s
	autorefresh=1
	type=rpm-md
	
#### Promote RPM through Stages

Now, it's time to deploy version 2.0 of your software RPM to the _releases_ repository. To install this RPM on your
_development_ machine, update your version alias first:

	curl -d "2.0" --header "Content-Type: text/plain" http://your.nexus.domain/nexus/service/local/yum/alias/releases/development/
	
Afterwards, login to your machine and update your software RPM via

	sudo yum update
	
After your integration or whatever tests verified the _development_ machine, you can promote the RPM to _verification_ stage:

	curl -d "2.0" --header "Content-Type: text/plain" http://your.nexus.domain/nexus/service/local/yum/alias/releases/verification/
	
Run again _sudo yum update_ on the _verification_ machine and test your software RPM in that environment. 
Finally, if everything is green, you can stage this RPM to _production_.

	curl -d "2.0" --header "Content-Type: text/plain" http://your.nexus.domain/nexus/service/local/yum/alias/releases/production/

and run _sudo yum update_ on your _production_ machine. 

#### Summary

Version aliases let you create versionized views on your Maven RPM repositories, which is useful your RPM staging, but
don't let you stage your whole bunch of artifacts like RPMs, JARs, WARs, Docs, etc. together. For this you need _Nexus Professional_.

## Help & Issues

Ask for help at our [Google Group][7] or [create a new issue][8].

## How to build

The build process is based on [Apache Maven 3][3]. You must have [createrepo][10] installed in order to execute all 
the integration tests. Just do a

    mvn package 

to run all tests and create a plugin bundle.

[1]: https://raw.github.com/sonatype/nexus-yum-plugin/master/docs/images/NeuxsYumPlugin.png
[2]: http://nexus.sonatype.org
[3]: http://maven.apache.org
[4]: http://www.immobilienscout24.de
[5]: http://blog.schlomo.schapiro.org/2011/05/configuration-management-with.html
[6]: http://www.slideshare.net/actionjackx/automated-java-deployments-with-rpm
[7]: https://groups.google.com/group/nexus-yum-plugin/
[8]: https://github.com/sonatype/nexus-yum-plugin/issues/new
[9]: http://www.sonatype.com/books/nexus-book/reference/install-sect-install.html
[10]: http://createrepo.baseurl.org/
[11]: http://code.google.com/p/nexus-yum-plugin/issues/detail?id=4
[12]: http://mojo.codehaus.org/rpm-maven-plugin/
[13]: http://www.sonatype.com/books/nexus-book/reference/staging-sect-intro.html
[14]: http://www.sonatype.com/books/nexus-book/reference/staging-sect-prepare-nexus.html
[15]: http://linux.die.net/man/1/mergerepo
[16]: https://raw.github.com/sonatype/nexus-yum-plugin/master/docs/images/Config-NexusCapabilities.png
[17]: https://raw.github.com/sonatype/nexus-yum-plugin/master/docs/images/Config-3rdPartyCapability.png
[18]: https://raw.github.com/sonatype/nexus-yum-plugin/master/docs/images/Config-GroupRepo.png


