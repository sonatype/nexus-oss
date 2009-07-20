package org.sonatype.nexus.mime;

import org.codehaus.plexus.PlexusTestCase;

public class DefaultMimeUtilTest
    extends PlexusTestCase
{
    protected DefaultMimeUtil defaultMimeUtil;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultMimeUtil = (DefaultMimeUtil) lookup( MimeUtil.class );
    }

    public void testSimple()
        throws Exception
    {
        assertEquals( "application/xml", defaultMimeUtil.getMimeType( getTestFile( "pom.xml" ) ) );
        assertEquals( "application/xml", defaultMimeUtil.getMimeType( getTestFile( "pom.xml" ).toURI().toURL() ) );

        assertEquals( "text/plain", defaultMimeUtil.getMimeType( getTestFile( "src/test/java/org/sonatype/nexus/mime/DefaultMimeUtilTest.java" ) ) );
        assertEquals( "application/x-java-class", defaultMimeUtil.getMimeType( getTestFile( "target/test-classes/org/sonatype/nexus/mime/DefaultMimeUtilTest.class" ) ) );
    }
}
