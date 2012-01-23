/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

public class DefaultMimeSupportTest
    extends PlexusTestCaseSupport
{

    protected MimeSupport mimeSupport;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        mimeSupport = lookup( MimeSupport.class );
    }

    /**
     * Tests the simple "guessing" against some known paths.
     */
    @Test
    public void testGuessMimeTypeFromPath()
    {
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/artifact.pom" ), equalTo( "application/xml" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/artifact.jar" ),
            equalTo( "application/java-archive" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/artifact-sources.jar" ),
            equalTo( "application/java-archive" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/maven-metadata.xml" ), equalTo( "application/xml" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.xml" ), equalTo( "application/xml" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.tar.gz" ), equalTo( "application/x-gzip" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.tar.bz2" ), equalTo( "application/x-bzip" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.zip" ), equalTo( "application/zip" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.war" ), equalTo( "application/zip" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.ear" ), equalTo( "application/zip" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( "/some/path/some.ejb" ), equalTo( "application/zip" ) );
    }

    /**
     * Tests that repo with diverting MimeRulesSupport actually works. If both tests, this one and
     * {@link #testGuessMimeTypeFromPath()} passes, their conjunction proves it works.
     */
    @Test
    public void testGuessfakeMimeRulesSourceMimeTypeFromPath()
    {
        MimeRulesSource fakeMimeRulesSource = new MimeRulesSource()
        {
            @Override
            public String getRuleForPath( String path )
            {
                return "foo/bar";
            }
        };

        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/artifact.pom" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/artifact.jar" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/artifact-sources.jar" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/maven-metadata.xml" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.xml" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.tar.gz" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.tar.bz2" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.zip" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.war" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.ear" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessMimeTypeFromPath( fakeMimeRulesSource, "/some/path/some.ejb" ),
            equalTo( "foo/bar" ) );
    }

    @Test
    public void testGuessNullMimeRulesSourceMimeTypeFromPath()
    {
        assertThat( mimeSupport.guessMimeTypeFromPath( null, "/some/path/artifact.pom" ), equalTo( "application/xml" ) );
    }
}
