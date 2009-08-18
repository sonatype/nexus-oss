package org.sonatype.nexus.plugins.lvo.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.lvo.NoSuchKeyException;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.plugins.lvo.config.model.Configuration;
import org.sonatype.nexus.plugins.lvo.config.model.io.xpp3.NexusLvoPluginConfigurationXpp3Reader;
import org.sonatype.nexus.plugins.lvo.config.model.io.xpp3.NexusLvoPluginConfigurationXpp3Writer;

@Component( role = LvoPluginConfiguration.class )
public class DefaultLvoPluginConfiguration
    extends AbstractLogEnabled
    implements LvoPluginConfiguration
{
    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}/conf/lvo-plugin.xml" )
    private File configurationFile;

    private Configuration configuration;

    private ReentrantLock lock = new ReentrantLock();

    public CLvoKey getLvoKey( String key )
        throws NoSuchKeyException
    {
        if ( StringUtils.isEmpty( key ) )
        {
            throw new NoSuchKeyException( key );
        }

        try
        {
            Configuration c = getConfiguration();

            for ( CLvoKey lvoKey : (List<CLvoKey>) c.getLvoKeys() )
            {
                if ( key.equals( lvoKey.getKey() ) )
                {
                    return lvoKey;
                }
            }

            throw new NoSuchKeyException( key );
        }
        catch ( Exception e )
        {
            throw new NoSuchKeyException( key );
        }
    }

    public boolean isEnabled()
    {
        try
        {
            return getConfiguration().isEnabled();
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "Unable to read configuration", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to read configuration", e );
        }

        return false;
    }

    public void enable()
        throws ConfigurationException,
            IOException
    {
        getConfiguration().setEnabled( true );
    }

    public void disable()
        throws ConfigurationException,
            IOException
    {
        getConfiguration().setEnabled( false );
    }

    protected Configuration getConfiguration()
        throws ConfigurationException,
            IOException
    {
        if ( configuration != null )
        {
            return configuration;
        }

        lock.lock();

        Reader fr = null;

        FileInputStream is = null;

        try
        {
            is = new FileInputStream( configurationFile );

            NexusLvoPluginConfigurationXpp3Reader reader = new NexusLvoPluginConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        catch ( FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            if ( !configurationFile.exists() )
            {
                copyFromStreamToFile(
                    getClass().getResourceAsStream( "/META-INF/nexus-lvo-plugin/lvo-plugin.xml" ),
                    configurationFile );

                return getConfiguration();
            }
            else
            {
                throw e;
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while retrieving configuration file", e );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        finally
        {
            if ( fr != null )
            {
                try
                {
                    fr.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }

        return configuration;
    }

    protected void save()
        throws IOException
    {
        lock.lock();

        configurationFile.getParentFile().mkdirs();

        Writer fw = null;

        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( configurationFile ) );

            NexusLvoPluginConfigurationXpp3Writer writer = new NexusLvoPluginConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.flush();

                    fw.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }
    }

    protected void clearCache()
    {
        configuration = null;
    }

    /**
     * Method copied from nexus-utils 1.3.0-SNAPSHOT. Remove this and use it when there.
     * 
     * @param is
     * @param output
     * @throws IOException
     * @deprecated Method copied from nexus-utils 1.3.0-SNAPSHOT. Remove this and use it when there.
     */
    public static void copyFromStreamToFile( InputStream is, File output )
        throws IOException
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream( output );

            org.codehaus.plexus.util.IOUtil.copy( is, fos );
        }
        finally
        {
            org.codehaus.plexus.util.IOUtil.close( is );

            org.codehaus.plexus.util.IOUtil.close( fos );
        }
    }

}
