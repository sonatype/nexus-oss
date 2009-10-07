package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;

public class SecurityXmlHandler
    extends AbstractXmlHandler
{
    public File getFile( SecurityModelConfigurationSource source, NexusConfiguration nexusConfig )
        throws IOException
    {
        Configuration configuration = 
            ( Configuration ) cloneViaXml( source.getConfiguration() );
        
        // No config ?
        if ( configuration == null )
        {
            return null;
        }
        
        for ( CUser user : ( List<CUser> ) configuration.getUsers() )
        {
            user.setPassword( PASSWORD_MASK );
            user.setEmail( PASSWORD_MASK );
        }
        
        SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "security.xml." + System.currentTimeMillis() );
            fWriter = new FileWriter( tempFile );
            writer.write( fWriter, configuration );
        }
        finally
        {
            if ( fWriter != null )
            {
                fWriter.close();
            }
        }
        
        return tempFile;
    }
}
