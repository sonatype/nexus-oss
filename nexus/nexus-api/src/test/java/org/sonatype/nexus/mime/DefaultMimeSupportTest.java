/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.repository.Repository;
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
    public void testGuessRepositoryMimeTypeFromPath()
    {
        MimeRulesSource fakeMimeRulesSource = new MimeRulesSource()
        {
            @Override
            public String getRuleForPath( String path )
            {
                return "foo/bar";
            }
        };
        Repository repository = Mockito.mock( Repository.class );
        Mockito.when( repository.getMimeRulesSource() ).thenReturn( fakeMimeRulesSource );

        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/artifact.pom" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/artifact.jar" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/artifact-sources.jar" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/maven-metadata.xml" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.xml" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.tar.gz" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.tar.bz2" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.zip" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.war" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.ear" ),
            equalTo( "foo/bar" ) );
        assertThat( mimeSupport.guessRepositoryMimeTypeFromPath( repository, "/some/path/some.ejb" ),
            equalTo( "foo/bar" ) );
    }

    @Test
    public void testGuessNullMimeRulesSourceMimeTypeFromPath()
    {
        assertThat( mimeSupport.guessMimeTypeFromPath( null, "/some/path/artifact.pom" ), equalTo( "application/xml" ) );
    }
}
