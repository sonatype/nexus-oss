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

public class M1GavCalculatorTest
    extends TestCase
{
    private M1GavCalculator gavCalculator;

    private SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd.HHmmss" );

    public void setUp()
        throws Exception
    {
        super.setUp();

        gavCalculator = new M1GavCalculator();
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
        Gav gav = gavCalculator.pathToGav( "/org.jruby/javadocs/jruby-1.0RC1-SNAPSHOT-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( "javadoc", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-SNAPSHOT-javadoc.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        String path = gavCalculator.gavToPath( gav );
        assertEquals( "/org.jruby/javadocs/jruby-1.0RC1-SNAPSHOT-javadoc.jar", path );

        gav = gavCalculator.pathToGav( "/org.jruby/jars/jruby-1.0RC1-SNAPSHOT.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-SNAPSHOT.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org.jruby/jars/jruby-1.0RC1-SNAPSHOT.jar", path );

        gav = gavCalculator.pathToGav( "/org.jruby/jars/jruby-1.0RC1-SNAPSHOT.jar.md5" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "jruby-1.0RC1-SNAPSHOT.jar.md5", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isHash() );
        assertEquals( Gav.HashType.md5, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/org.jruby/jars/jruby-1.0RC1-SNAPSHOT.jar.md5", path );

        gav = gavCalculator.pathToGav( "/org.jruby/javadocs/jruby-1.0-javadoc.jar" );

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
        assertEquals( "/org.jruby/javadocs/jruby-1.0-javadoc.jar", path );

        gav = gavCalculator.pathToGav( "/org.jruby/javadocs/jruby-1.0-javadoc.jar.sha1" );

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
        assertEquals( "/org.jruby/javadocs/jruby-1.0-javadoc.jar.sha1", path );

        gav = gavCalculator.pathToGav( "/org.jruby/jars/jruby-1.0.jar" );

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
        assertEquals( "/org.jruby/jars/jruby-1.0.jar", path );

        gav = gavCalculator.pathToGav( "/maven/jars/dom4j-1.7-20060614.jar" );

        assertEquals( "maven", gav.getGroupId() );
        assertEquals( "dom4j", gav.getArtifactId() );
        assertEquals( "1.7-20060614", gav.getVersion() );
        assertEquals( "1.7-20060614", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "dom4j-1.7-20060614.jar", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/maven/jars/dom4j-1.7-20060614.jar", path );

        gav = gavCalculator.pathToGav( "maven/java-sources/velocity-1.5-SNAPSHOT-sources.jar" );

        assertEquals( "maven", gav.getGroupId() );
        assertEquals( "velocity", gav.getArtifactId() );
        assertEquals( "1.5-SNAPSHOT", gav.getVersion() );
        assertEquals( "1.5-SNAPSHOT", gav.getBaseVersion() );
        assertEquals( "sources", gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "velocity-1.5-SNAPSHOT-sources.jar", gav.getName() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/maven/java-sources/velocity-1.5-SNAPSHOT-sources.jar", path );

        gav = gavCalculator.pathToGav( "castor/jars/castor-0.9.9-xml.jar" );

        assertEquals( "castor", gav.getGroupId() );
        assertEquals( "castor", gav.getArtifactId() );
        assertEquals( "0.9.9-xml", gav.getVersion() );
        assertEquals( "0.9.9-xml", gav.getBaseVersion() );
        assertEquals( null, gav.getClassifier() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "castor-0.9.9-xml.jar", gav.getName() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isHash() );
        assertEquals( null, gav.getHashType() );

        path = gavCalculator.gavToPath( gav );
        assertEquals( "/castor/jars/castor-0.9.9-xml.jar", path );
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

        gav = gavCalculator.pathToGav( "/something/that/like-an-artifact.blah" );
        assertEquals( null, gav );
        // assertEquals( false, gav.isChecksum() );
        // assertEquals( false, gav.isPrimary() );
        // assertEquals( false, gav.isSnapshot() );

        gav = gavCalculator.pathToGav( "/something/that/like-an-artifact.pom" );
        assertEquals( null, gav );
        // assertEquals( false, gav.isChecksum() );
        // assertEquals( false, gav.isPrimary() );
        // assertEquals( false, gav.isSnapshot() );

        gav = gavCalculator.pathToGav( "/something/that/maven-metadata.xml" );
        assertEquals( null, gav );
        // assertEquals( false, gav.isChecksum() );
        // assertEquals( false, gav.isPrimary() );
        // assertEquals( false, gav.isSnapshot() );

        gav = gavCalculator.pathToGav( "/something/that/like-SNAPSHOT/maven-metadata.xml" );
        assertEquals( null, gav );

    }
}
