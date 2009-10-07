package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;

public class SecurityConfigurationXmlHandler
    extends AbstractXmlHandler
{
    public File getFile( SecurityConfigurationSource source, NexusConfiguration nexusConfig )
        throws IOException
    {
        SecurityConfiguration configuration = ( SecurityConfiguration )cloneViaXml( source.getConfiguration() );
        
        // No config ??
        if ( configuration == null )
        {
            return null;
        }
        
        configuration.setAnonymousPassword( PASSWORD_MASK );
        
        SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();
        
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "security-configuration.xml." + System.currentTimeMillis() );
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
