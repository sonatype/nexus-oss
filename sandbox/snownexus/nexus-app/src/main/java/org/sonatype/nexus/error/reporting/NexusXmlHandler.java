package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;

public class NexusXmlHandler
    extends AbstractXmlHandler
{
    public File getFile( ConfigurationHelper configHelper, NexusConfiguration nexusConfig )
        throws IOException
    {
        Configuration configuration = configHelper.clone( nexusConfig.getConfigurationModel() );
        
        // No config ?
        if ( configuration == null )
        {
            return null;
        }
        
        configHelper.maskPasswords( configuration );
        
        NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "nexus.xml." + System.currentTimeMillis() );
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
