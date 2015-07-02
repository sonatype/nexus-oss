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
# Sonatype Nexus OSS

Sonatype Nexus - Open Source Edition

## Building

### Requirements

* Apache Maven 3.0.4+
* Java 7+
* Groovy 1.7+ (for advanced usage only)

## Running

To run Nexus, after building, unzip the assembly and start the server:

    unzip -d target assemblies/nexus-base-template/target/nexus-base-template-*.zip
    ./target/nexus-base-template-*/bin/nexus console

The `nexus-base-template` assembly is used as the basis for the official Sonatype Nexus distributions.

## HOWTO

Some examples may require Groovy 1.7+

## Skip Modules

The plugins and testsuite modules can be skipped:

    mvn -Dskip-plugins

and:

    mvn -Dskip-testsuite

## Manage License Headers

Check for violations:

    mvn -Plicense-check -N

Apply header format:

    mvn -Plicense-format -N

## Normalize Line Endings

Normalize line-endings (to UNIX LF style), from project root directory:

    groovy ./buildsupport/scripts/fixcrlf.groovy

Same with extra configuration ( -D must be _before_ script ):

    groovy -Dfixlast=true ./buildsupport/scripts/fixcrlf.groovy

## Running Custom Testsuite Shards

To run all capabilities tests in modern testsuite:

    mvn -pl testsuite/modern-testsuite/pom.xml clean install \
        -Pit,testsuite-gencustom,testsuite-runcustom \
        -Dautoshard.includes='**/capabilities/**/*IT.java'



