package org.sonatype.security.configuration.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.source.AbstractStreamConfigurationSource;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer;

/**
 * Abstract class that encapsulates Modello model.
 * 
 * @author tstevens
 */
public abstract class AbstractSecurityConfigurationSource
    extends AbstractStreamConfigurationSource<SecurityConfiguration>
    implements SecurityConfigurationSource
{

    /** The configuration. */
    private SecurityConfiguration configuration;

    public SecurityConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( SecurityConfiguration configuration )
    {
        this.configuration = configuration;
    }

    /**
     * Called by subclasses when loaded configuration is rejected for some reason.
     */
    protected void rejectConfiguration( String message )
    {
        this.configuration = null;

        if ( message != null )
        {
            getLogger().warn( message );
        }
    }

    /**
     * Load configuration.
     * 
     * @param file the file
     * @return the configuration
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void loadConfiguration( InputStream is )
        throws IOException
    {
        setConfigurationUpgraded( false );

        Reader fr = null;

        try
        {
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        catch ( XmlPullParserException e )
        {
            rejectConfiguration( "Security configuration file was not loaded, it has the wrong structure." );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "security.xml is broken:", e );
            }
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }

        // check the model version if loaded
        if ( configuration != null && !SecurityConfiguration.MODEL_VERSION.equals( configuration.getVersion() ) )
        {
            rejectConfiguration( "Security configuration file was loaded but discarded, it has the wrong version number." );
        }

        if ( getConfiguration() != null )
        {
            getLogger().debug( "Configuration loaded succesfully." );
        }
    }

    /**
     * Save configuration.
     * 
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void saveConfiguration( OutputStream os, SecurityConfiguration configuration )
        throws IOException
    {
        Writer fw = null;
        try
        {
            fw = new OutputStreamWriter( os );

            SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();

                fw.close();
            }
        }
    }
}
