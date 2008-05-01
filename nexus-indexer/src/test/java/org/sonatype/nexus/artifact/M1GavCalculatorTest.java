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

public class M1GavCalculatorTest
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
        Gav gav = M1GavCalculator.calculate( "/org.jruby/javadocs/jruby-1.0RC1-SNAPSHOT-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0RC1-SNAPSHOT-javadoc.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "javadoc", gav.getClassifier() );

        String path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org.jruby/poms/jruby-1.0RC1-SNAPSHOT.pom", path );

        gav = M1GavCalculator.calculate( "/org.jruby/jars/jruby-1.0RC1-SNAPSHOT.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0RC1-SNAPSHOT.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org.jruby/poms/jruby-1.0RC1-SNAPSHOT.pom", path );

        gav = M1GavCalculator.calculate( "/org.jruby/jars/jruby-1.0RC1-SNAPSHOT.jar.md5" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0RC1-SNAPSHOT", gav.getVersion() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( true, gav.isChecksum() );
        assertEquals( "jruby-1.0RC1-SNAPSHOT.jar.md5", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org.jruby/poms/jruby-1.0RC1-SNAPSHOT.pom", path );
        
        gav = M1GavCalculator.calculate( "/org.jruby/javadocs/jruby-1.0-javadoc.jar" );

        assertEquals( "org.jruby", gav.getGroupId() );
        assertEquals( "jruby", gav.getArtifactId() );
        assertEquals( "1.0", gav.getVersion() );
        assertEquals( "jar", gav.getExtension() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "jruby-1.0-javadoc.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "javadoc", gav.getClassifier() );

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org.jruby/poms/jruby-1.0.pom", path );

        gav = M1GavCalculator.calculate( "/org.jruby/javadocs/jruby-1.0-javadoc.jar.sha1" );

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

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org.jruby/poms/jruby-1.0.pom", path );

        gav = M1GavCalculator.calculate( "/org.jruby/jars/jruby-1.0.jar" );

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

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/org.jruby/poms/jruby-1.0.pom", path );

        gav = M1GavCalculator.calculate( "/maven/jars/dom4j-1.7-20060614.jar" );

        assertEquals( "maven", gav.getGroupId() );
        assertEquals( "dom4j", gav.getArtifactId() );
        assertEquals( "1.7-20060614", gav.getVersion() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "dom4j-1.7-20060614.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/maven/poms/dom4j-1.7-20060614.pom", path );

        gav = M1GavCalculator.calculate( "maven/java-sources/velocity-1.5-SNAPSHOT-sources.jar" );

        assertEquals( "maven", gav.getGroupId() );
        assertEquals( "velocity", gav.getArtifactId() );
        assertEquals( "1.5-SNAPSHOT", gav.getVersion() );
        assertEquals( false, gav.isPrimary() );
        assertEquals( true, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "velocity-1.5-SNAPSHOT-sources.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( "sources", gav.getClassifier() );

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/maven/poms/velocity-1.5-SNAPSHOT.pom", path );

        gav = M1GavCalculator.calculate( "castor/jars/castor-0.9.9-xml.jar" );

        assertEquals( "castor", gav.getGroupId() );
        assertEquals( "castor", gav.getArtifactId() );
        assertEquals( "0.9.9-xml", gav.getVersion() );
        assertEquals( true, gav.isPrimary() );
        assertEquals( false, gav.isSnapshot() );
        assertEquals( false, gav.isChecksum() );
        assertEquals( "castor-0.9.9-xml.jar", gav.getName() );
        assertEquals( null, gav.getSnapshotBuildNumber() );
        assertEquals( null, gav.getSnapshotTimeStamp() );
        assertEquals( null, gav.getClassifier() );

        path = M1GavCalculator.calculateRepositoryPath( gav );
        assertEquals( "/castor/poms/castor-0.9.9-xml.pom", path );

    }

    public void testGavExtreme()
        throws Exception
    {
        Gav gav = M1GavCalculator.calculate( "/" );

        assertEquals( null, gav );

        gav = M1GavCalculator.calculate( "/some/stupid/path" );
        assertEquals( null, gav );

        gav = M1GavCalculator.calculate( "/some/stupid/path/more/in/it" );
        assertEquals( null, gav );

        gav = M1GavCalculator.calculate( "/something/that/looks/" );
        assertEquals( null, gav );

        gav = M1GavCalculator.calculate( "/something/that/like-an-artifact.blah" );
        assertEquals( null, gav );
        // assertEquals( false, gav.isChecksum() );
        // assertEquals( false, gav.isPrimary() );
        // assertEquals( false, gav.isSnapshot() );

        gav = M1GavCalculator.calculate( "/something/that/like-an-artifact.pom" );
        assertEquals( null, gav );
        // assertEquals( false, gav.isChecksum() );
        // assertEquals( false, gav.isPrimary() );
        // assertEquals( false, gav.isSnapshot() );

        gav = M1GavCalculator.calculate( "/something/that/maven-metadata.xml" );
        assertEquals( null, gav );
        // assertEquals( false, gav.isChecksum() );
        // assertEquals( false, gav.isPrimary() );
        // assertEquals( false, gav.isSnapshot() );

        gav = M1GavCalculator.calculate( "/something/that/like-SNAPSHOT/maven-metadata.xml" );
        assertEquals( null, gav );

    }
}
