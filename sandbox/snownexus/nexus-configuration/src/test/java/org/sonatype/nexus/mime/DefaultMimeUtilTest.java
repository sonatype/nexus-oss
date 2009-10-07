package org.sonatype.nexus.mime;

import java.io.File;
import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;

public class DefaultMimeUtilTest
    extends PlexusTestCase
{
    protected MimeUtil mimeUtil;

    protected MimeUtil2 medseaMimeUtil;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        mimeUtil = lookup( MimeUtil.class );

        medseaMimeUtil = new MimeUtil2();

        medseaMimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );
    }

    @SuppressWarnings( "unchecked" )
    protected String getMimeType( File file )
    {
        Collection<MimeType> mimeTypes = medseaMimeUtil.getMimeTypes( file );

        return MimeUtil2.getMostSpecificMimeType( mimeTypes ).toString();
    }

    protected String getMimeType( String fileName )
    {
        Collection<MimeType> mimeTypes = medseaMimeUtil.getMimeTypes( fileName );

        return MimeUtil2.getMostSpecificMimeType( mimeTypes ).toString();
    }

    public void testSimple()
        throws Exception
    {
        File testFile = null;

        testFile = getTestFile( "pom.xml" );
        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile.toURI().toURL() ) );

        testFile = getTestFile( "src/test/java/org/sonatype/nexus/mime/DefaultMimeUtilTest.java" );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );

        testFile = getTestFile( "target/test-classes/org/sonatype/nexus/mime/DefaultMimeUtilTest.class" );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );
    }

    public void testSimpleByName()
        throws Exception
    {
        String testFileName = null;

        testFileName = "pom.xml";
        
        assertEquals( getMimeType( testFileName ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "/some/path/pom.xml";
        
        assertEquals( getMimeType( "pom.xml" ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "\\some\\path\\pom.xml";
        
        assertEquals( getMimeType( "pom.xml" ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "DefaultMimeUtilTest.java";

        assertEquals( getMimeType( testFileName ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "DefaultMimeUtilTest.class";

        assertEquals( getMimeType( testFileName ), mimeUtil.getMimeType( testFileName ) );
    }
}
