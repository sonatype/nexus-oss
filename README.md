# Nexus Yum Plugin

A plugin for Sonatype Nexus 1.9.2.x or 2.X which recognizes RPMs in Nexus Maven repositories and generates 
Yum metadata, so that RedHat-compatible system can use Nexus as software repository. 

## Audience

The Nexus Yum Plugin is for all guys, who are deploying Java application for RedHat-compatible (RHEL, Centos, Fedora) servers and deploy via RPM.

### Default use case

![CLD at IS24][1]

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

## Help & Issues

Ask for help at our [Google Group][7] or [create a new issue][8].

### Installation

1. [Install Sonatype Nexus][9]
1. Download latest *nexus-yum-plugin-bundle.zip* from our downloads page
1. Unzip the bundle to *$NEXUS_WORK_DIR/plugin-repository/*. The default for *$NEXUS_WORK_DIR* is *~/sonatype-work/nexus/*. For example:
    unzip nexus-yum-plugin-1.13-bundle-zip -d $NEXUS_WORK_DIR/plugin-repository/
1. Install [createrepo][10] using your update manager (*yum*, *apt-get*, etc.) eg.
    sudo yum install createrepo
1. Make sure that in *Nexus Adminstration --> Settings --> Application Server Settings (optional) --> Base URL* is set to a vaild URL like :
    http://your.nexus.domain:8081/nexus
1. Sometimes *Force Base URL* is nessessary, too, see [ISSUE 4][11] . Otherwise the plugin can't determine the server URL and you got RPM locations like *null/content/repositories/*... in *primary.xml*.
1. Configure Nexus Yum Plugin via *yum.xml*. See Configuration.
1. Restart Nexus. Eg.
    sudo service nexus stop
    sudo service nexus start

Now the plugin should be installed.

## Configuration

Here, you'll find everything about configuring Nexus Yum Plugin.

### Location

The configuration of the Nexus Yum Plugin can be found in *yum.xml* in the same directory as *nexus.xml* :
	$NEXUS_WORK_DIR/conf/yum.xml
Default:
	~/sonatype-work/nexus/conf/yum.xml

### Example

Example *yum.xml*:

	<?xml version="1.0" encoding="UTF-8"?>
	<configuration>
	  <!-- timeout for requests for a filtered (versionized) repository -->
	  <repositoryCreationTimeout>150</repositoryCreationTimeout><!-- in seconds -->
	  
	  <!-- enables or disables the creation of a repository of repositories -->
	  <repositoryOfRepositoryVersionsActive>true</repositoryOfRepositoryVersionsActive>
	  
	  <!-- enables or disables of delete rpm events via nexus -->
	  <deleteProcessing>true</deleteProcessing>
	  
	  <!-- delay after which the rebuild of a repository is triggered in which one or more rpms got deleted -->
	  <delayAfterDeletion>10</delayAfterDeletion><!-- in seconds -->
	  
	  <!-- configured aliases -->
	  <aliasMappings>
	    <aliasMapping>
	      <repoId>releases</repoId>
	      <alias>trunk</alias>
	      <version>5.1.15-2</version>
	    </aliasMapping>
	    <aliasMapping>
	      <repoId>releases</repoId>
	      <alias>production</alias>
	      <version>5.1.15-1</version>
	    </aliasMapping>
	  </aliasMappings>
	</configuration>

## How to build

The build process is based on [Apache Maven 3][3]. You must have [createrepo][10] installed in order to execute all the integration tests. Just do a

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
