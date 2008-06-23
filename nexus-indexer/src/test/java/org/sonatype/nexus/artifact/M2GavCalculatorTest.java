/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.artifact;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

public class M2GavCalculatorTest
    extends TestCase
{
    private M2GavCalculator gavCalculator;

    private SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd.HHmmss" );

    public void setUp()
        throws Exception
    {
        super.setUp();

        gavCalculator = new M2GavCalculator();
    }

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

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-25-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-20070504.160758-25", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( "javadoc", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( Integer.valueOf( 25 ), gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-20070504.160758-25-javadoc.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-25-javadoc.jar", path );

        gav = gavCalculator
            .pathToGav( "/com/sun/xml/ws/jaxws-local-transport/2.1.3/jaxws-local-transport-2.1.3.pom.md5" );

        assertEquals( "com.sun.xml.ws", gav.getGroupId() );
        assertEquals( "jaxws-local-transport", gav.getArtifactId() );
        assertEquals( "2.1.3", gav.getVersion() );
        assertEquals( "2.1.3", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "pom", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jaxws-local-transport-2.1.3.pom.md5", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( true, gav.isHash() );
        assertEquals( Gav.HashType.md5, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/com/sun/xml/ws/jaxws-local-transport/2.1.3/jaxws-local-transport-2.1.3.pom.md5", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-20070504.160758-2", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( Integer.valueOf( 2 ), gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-20070504.160758-2.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar.md5" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-20070504.160758-2", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( Integer.valueOf( 2 ), gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-20070504.160758-2.jar.md5", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isHash() );
        assertEquals( Gav.HashType.md5, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar.md5", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-20070504.160758-2", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( Integer.valueOf( 2 ), gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-20070504.160758-2.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2.jar", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2-sources.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-20070504.160758-2", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( "sources", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( Integer.valueOf( 2 ), gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20070504.160758" ), gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-20070504.160758-2-sources.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0RC1-SNAPSHOT/jruby-1.0RC1-20070504.160758-2-sources.jar", path );

        gav = gavCalculator
            .pathToGav( "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.jar.sha1" );

        assertEquals( "com.stchome.products.dsms.services", gav.getGroupId() );
        assertEquals( "dsms-intervention-service", gav.getArtifactId() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getVersion() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "dsms-intervention-service-2.4.2-64-SNAPSHOT.jar.sha1", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isHash() );
        assertEquals( Gav.HashType.sha1, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals(
            "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.jar.sha1",
            path );

        gav = gavCalculator
            .pathToGav( "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT-javadoc.jar.sha1" );

        assertEquals( "com.stchome.products.dsms.services", gav.getGroupId() );
        assertEquals( "dsms-intervention-service", gav.getArtifactId() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getVersion() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( "javadoc", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "dsms-intervention-service-2.4.2-64-SNAPSHOT-javadoc.jar.sha1", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isHash() );
        assertEquals( Gav.HashType.sha1, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals(
            "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT-javadoc.jar.sha1",
            path );

        gav = gavCalculator
            .pathToGav( "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.jar" );

        assertEquals( "com.stchome.products.dsms.services", gav.getGroupId() );
        assertEquals( "dsms-intervention-service", gav.getArtifactId() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getVersion() );
        assertEquals( "2.4.2-64-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "dsms-intervention-service-2.4.2-64-SNAPSHOT.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals(
            "/com/stchome/products/dsms/services/dsms-intervention-service/2.4.2-64-SNAPSHOT/dsms-intervention-service-2.4.2-64-SNAPSHOT.jar",
            path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0/jruby-1.0-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( "1.0", gav.getBaseVersion() );
        assertEquals( "javadoc", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0-javadoc.jar", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0/jruby-1.0-javadoc.jar", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0/jruby-1.0-javadoc.jar.sha1" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( "1.0", gav.getBaseVersion() );
        assertEquals( "javadoc", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0-javadoc.jar.sha1", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( true, gav.isHash() );
        assertEquals( Gav.HashType.sha1, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0/jruby-1.0-javadoc.jar.sha1", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0/jruby-1.0.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( "1.0", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0.jar", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org/jruby/jruby/1.0/jruby-1.0.jar", path );

        gav = gavCalculator.pathToGav( "/activemq/activemq-core/1.2/activemq-core-1.2.pom" );

        assertEquals( "activemq", gav.getGroupId() );
        assertEquals( "activemq-core", gav.getArtifactId() );
        assertEquals( "1.2", gav.getVersion() );
        assertEquals( "1.2", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "pom", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "activemq-core-1.2.pom", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/activemq/activemq-core/1.2/activemq-core-1.2.pom", path );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/org/jruby/jruby/1.0-SNAPSHOT/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/junit/junit/3.8/junit-3.8.jar" );
        assertEquals( "junit", gav.getGroupId() );
        assertEquals( "junit", gav.getArtifactId() );
        assertEquals( "3.8", gav.getVersion() );
        assertEquals( "3.8", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "junit-3.8.jar", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        gav = gavCalculator.pathToGav( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT.pom" );
        assertEquals( "foo1", gav.getGroupId() );
        assertEquals( "foo1", gav.getArtifactId() );
        assertEquals( "0.0.1-SNAPSHOT", gav.getVersion() );
        assertEquals( "0.0.1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "pom", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "foo1-0.0.1-SNAPSHOT.pom", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT.pom", path );

        gav = gavCalculator.pathToGav( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT-jdk14.jar" );
        assertEquals( "foo1", gav.getGroupId() );
        assertEquals( "foo1", gav.getArtifactId() );
        assertEquals( "0.0.1-SNAPSHOT", gav.getVersion() );
        assertEquals( "0.0.1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( "jdk14", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "foo1-0.0.1-SNAPSHOT-jdk14.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/foo1/foo1/0.0.1-SNAPSHOT/foo1-0.0.1-SNAPSHOT-jdk14.jar", path );

        gav = gavCalculator.pathToGav( "/foo1/foo1/1.0.0-beta-4-SNAPSHOT/foo1-1.0.0-beta-4-20080623.175436-1.jar" );
        assertEquals( "foo1", gav.getGroupId() );
        assertEquals( "foo1", gav.getArtifactId() );
        assertEquals( "1.0.0-beta-4-20080623.175436-1", gav.getVersion() );
        assertEquals( "1.0.0-beta-4-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( Integer.valueOf( 1 ), gav.getSnapshotBuildNumber() );
        assertEquals( parseTimestamp( "20080623.175436" ), gav.getSnapshotTimeStamp() );
        assertEquals( "foo1-1.0.0-beta-4-20080623.175436-1.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/foo1/foo1/1.0.0-beta-4-SNAPSHOT/foo1-1.0.0-beta-4-20080623.175436-1.jar", path );
        
        
    }

    public void testGavExtreme()
        throws Exception
    {
        Gav gav = gavCalculator.pathToGav( "/" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/some/stupid/path" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/some/stupid/path/more/in/it" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/something/that/looks/" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/something/that/looks/like-an-artifact.blah" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/something/that/looks/like-an-artifact.pom" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/something/that/looks/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/something/that/looks/like-SNAPSHOT/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "/org/codehaus/plexus/plexus-container-default/maven-metadata.xml" );
        assertEquals( null, gav );

        gav = gavCalculator.pathToGav( "org/apache/maven/scm/maven-scm" );
        assertEquals( null, gav );
    }

    public void testIssueNexus57()
    {
        Gav gav;
        // broken path, baseVersion and version mismatch (2.0-SNAPSHOT vs 2.0-alpha-1...)
        gav = gavCalculator
            .pathToGav( "/org/apache/maven/plugins/maven-dependency-plugin/2.0-SNAPSHOT/maven-dependency-plugin-2.0-alpha-1-20070109.165112-13.jar" );
        assertEquals( null, gav );
    }
}
