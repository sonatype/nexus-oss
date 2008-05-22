package org.sonatype.nexus.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.DefaultArchiverManager;

public abstract class AbstractNexusTest extends PlexusTestCase
{
    private String nexusUrl;
    
    public AbstractNexusTest( String nexusUrl )
    {
        this.nexusUrl = nexusUrl;
    }
    
    protected File downloadArtifact( String groupId, String artifact, String version, String type, String targetDirectory )
    {
        URL url = null;
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        
        new File( targetDirectory ).mkdirs();
        
        File downloadedFile = new File( targetDirectory + "/" + artifact + "-" + version + "." + type );
        try
        {
            url = new URL( nexusUrl + groupId.replace( '.', '/' ) + "/" + artifact + "/" + version + "/" + artifact + "-" + version + "." + type);
            out = new BufferedOutputStream( new FileOutputStream( downloadedFile ) );
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ( ( numRead = in.read( buffer ) ) != -1 )
            {
                out.write( buffer, 0, numRead );
                numWritten += numRead;
            }
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            assert( false );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            assert( false );
        }
        finally
        {
            try
            {
                if ( out != null )
                {
                    out.close();
                }
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( IOException e )
            {
            }

        }
        
        return downloadedFile;
    }
    
    protected File unpackArtifact( File artifact, String targetDirectory )
    {
        File target = null;
        try
        {
            target = new File( targetDirectory );
            target.mkdirs();
            ArchiverManager manager = (ArchiverManager) lookup( DefaultArchiverManager.ROLE );
            UnArchiver unarchiver = manager.getUnArchiver( artifact );
            unarchiver.setSourceFile( artifact );
            unarchiver.setDestDirectory( target );
            unarchiver.extract();            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            assert( false );
        }
        
        return target;
    }
}
