package org.sonatype.jsecurity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;

public abstract class AbstractSecurityTestCase
    extends PlexusTestCase
{
    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();
    
        Reader fr = new InputStreamReader( is );
    
        return reader.read( fr );
    }
}
