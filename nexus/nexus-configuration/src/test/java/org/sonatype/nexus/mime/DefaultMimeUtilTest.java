package org.sonatype.nexus.mime;

import org.codehaus.plexus.PlexusTestCase;

import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

public class DefaultMimeUtilTest
    extends PlexusTestCase
{
    protected DefaultMimeUtil defaultMimeUtil;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultMimeUtil = (DefaultMimeUtil) lookup( MimeUtil.class );
        
        // this is platform dependant (linux vs win vs mac), so for tests we use extension detector only,
        // to be sure about 100% same results on all platforms.
        // defaultMimeUtil.getMimeUtil2().unregisterMimeDetector( MagicMimeMimeDetector.class.getName() );
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
