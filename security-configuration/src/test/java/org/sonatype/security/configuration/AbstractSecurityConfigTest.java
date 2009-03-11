package org.sonatype.security.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

public class AbstractSecurityConfigTest
    extends PlexusTestCase
{

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File CONF_HOME = new File( PLEXUS_HOME, "conf" );

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/security/security.xml", getSecurityConfiguration() );
    }

    protected String getSecurityConfiguration()
    {
        return CONF_HOME + "/security.xml";
    }

    protected void copyResource( String resource, String dest )
        throws IOException
    {
        InputStream stream = null;
        FileOutputStream ostream = null;
        try
        {
            stream = getClass().getResourceAsStream( resource );
            ostream = new FileOutputStream( dest );
            IOUtil.copy( stream, ostream );
        }
        finally
        {
            IOUtil.close( stream );
            IOUtil.close( ostream );
        }
    }

    protected void copyFromClasspathToFile( String path, String outputFilename )
        throws IOException
    {
        copyFromClasspathToFile( path, new File( outputFilename ) );
    }

    protected void copyFromClasspathToFile( String path, File output )
        throws IOException
    {
        copyFromStreamToFile( getClass().getResourceAsStream( path ), output );
    }

    // this one may find its way back to plexus-utils, copied from IOUtil In nexus
    public static void copyFromStreamToFile( InputStream is, File output )
        throws IOException
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream( output );

            IOUtil.copy( is, fos );
        }
        finally
        {
            IOUtil.close( is );

            IOUtil.close( fos );
        }
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        // delete the config dir
        FileUtils.deleteDirectory( PLEXUS_HOME );
        
        // create conf dir
        CONF_HOME.mkdirs();
    }

}
