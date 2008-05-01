/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.artifact;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

public class M2GavCalculatorTest
    extends TestCase
{
    private SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd.HHmmss" );

    protected Long parseTimestamp( String timeStamp )
        throws ParseException
    {
        if ( timeStamp == null )
        {
            return null;
        }
        else
        {
            return Long.valueOf( formatter.parse( timeStamp ).getTime() );
        }
    }

    public void testGav()
        throws Exception
    {
        Gav gav;
        String path;

        gav = M2GavCalculator
            .calculate( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-25-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0RC1-20070504.160758-25-javadoc.jar", gav.getName() );
        assertEquals( "20070504.160758-25", gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( "javadoc", gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-25.pom", path );

        gav = M2GavCalculator
            .calculate( "/com/sun/xml/ws/jaxws-local-transport/2.1.3/jaxws-local-transport-2.1.3.pom.md5" );

        assertEquals( "com.sun.xml.ws", gav.getGroupId() );
        assertEquals( "jaxws-local-transport", gav.getArtifactId() );
        assertEquals( "2.1.3", gav.getVersion() );
        assertEquals( "pom", gav.getExtension() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( true, gav.isChecksum() );
        assertEquals( "jaxws-local-transport-2.1.3.pom.md5", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/com/sun/xml/ws/jaxws-local-transport/2.1.3/jaxws-local-transport-2.1.3.pom", path );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0RC1-20070504.160758-2.jar", gav.getName() );
        assertEquals( "20070504.160758-2", gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.pom", path );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar.md5" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isChecksum() );
        assertEquals( "jruby-1.0RC1-20070504.160758-2.jar.md5", gav.getName() );
        assertEquals( "20070504.160758-2", gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        gav = M2GavCalculator
            .calculate( "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.jar.sha1" );

        assertEquals( "com.stchome.products.dsms.services", gav.getGroupId() );
        assertEquals( "dsms-intervention-service", gav.getArtifactId() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getVersion() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isChecksum() );
        assertEquals( "dsms-intervention-service-2.4.2-64-SNAPSHOT.jar.sha1", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals(
            "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.pom",
            path );

        gav = M2GavCalculator
            .calculate( "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT-javadoc.jar.sha1" );

        assertEquals( "com.stchome.products.dsms.services", gav.getGroupId() );
        assertEquals( "dsms-intervention-service", gav.getArtifactId() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getVersion() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isChecksum() );
        assertEquals( "dsms-intervention-service-2.4.2-64-SNAPSHOT-javadoc.jar.sha1", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "javadoc", gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals(
            "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.pom",
            path );

        gav = M2GavCalculator
            .calculate( "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.jar" );

        assertEquals( "com.stchome.products.dsms.services", gav.getGroupId() );
        assertEquals( "dsms-intervention-service", gav.getArtifactId() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getVersion() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "dsms-intervention-service-2.4.2-64-SNAPSHOT.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals(
            "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.pom",
            path );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0/jruby-1.0-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0-javadoc.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "javadoc", gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org/jruby/jruby/1.0/jruby-1.0.pom", path );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0/jruby-1.0-javadoc.jar.sha1" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( true, gav.isChecksum() );
        assertEquals( "jruby-1.0-javadoc.jar.sha1", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "javadoc", gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org/jruby/jruby/1.0/jruby-1.0.pom", path );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0/jruby-1.0.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org/jruby/jruby/1.0/jruby-1.0.pom", path );

        gav = M2GavCalculator.calculate( "/activemq/activemq-core/1.2/activemq-core-1.2.pom" );

        assertEquals( "activemq", gav.getGroupId() );
        assertEquals( "activemq-core", gav.getArtifactId() );
        assertEquals( "1.2", gav.getVersion() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "activemq-core-1.2.pom", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/activemq/activemq-core/1.2/activemq-core-1.2.pom", path );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/org/jruby/jruby/1.0-SNAPSHOT/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/junit/junit/3.8/junit-3.8.jar" );
        assertNotNull( gav );
        assertEquals( "junit", gav.getGroupId() );
        assertEquals( "junit", gav.getArtifactId() );

        gav = M2GavCalculator.calculate( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT.pom" );
        assertNotNull( gav );
        assertEquals( "foo1", gav.getGroupId() );
        assertEquals( "foo1", gav.getArtifactId() );
        assertEquals( "0.0.1-SNAPSHOT", gav.getVersion() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT.pom", path );

        gav = M2GavCalculator.calculate( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT-jdk14.jar" );
        assertNotNull( gav );
        assertEquals( "foo1", gav.getGroupId() );
        assertEquals( "foo1", gav.getArtifactId() );
        assertEquals( "0.0.1-SNAPSHOT", gav.getVersion() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( "jdk14", gav.getClassifier() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );

        path = M2GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT.pom", path );

    }

    public void testGavExtreme()
        throws Exception
    {
        Gav gav = M2GavCalculator.calculate( "/" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/some/stupid/path" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/some/stupid/path/more/in/it" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/something/that/looks/" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/something/that/looks/like-an-artifact.blah" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/something/that/looks/like-an-artifact.pom" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/something/that/looks/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/something/that/looks/like-SNAPSHOT/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = M2GavCalculator.calculate( "/org/codehaus/plexus/plexus-container-default/maven-metadata.xml" );
        assertEquals( null, gav );

    }
}
