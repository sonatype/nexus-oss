package org.sonatype.nexus.plugin.migration.artifactory.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.Configuration;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

@Component( role = MappingConfiguration.class, hint = "default" )
public class DefaultMappingConfiguration
    extends AbstractLogEnabled
    implements MappingConfiguration
{

    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}/conf/mapping.xml" )
    private File configurationFile;

    private ReentrantLock lock = new ReentrantLock();

    private Configuration configuration;

    public void addMapping( CMapping map )
    {
        lock.lock();
        try
        {
            getConfiguration().addUrlMapping( map );

            save();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void save()
    {
        lock.lock();
        try
        {
            XStream xs = getXStream();

            Writer out = new FileWriter( configurationFile );
            xs.toXML( getConfiguration(), out );
            out.close();
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to save mapping configuration", e );
        }
        finally
        {
            lock.unlock();
        }
    }

    private XStream getXStream()
    {
        XStream xs = new XStream();
        xs.processAnnotations( Configuration.class );
        xs.processAnnotations( CMapping.class );
        return xs;
    }

    private Configuration getConfiguration()
    {
        if ( this.configuration != null )
        {
            return this.configuration;
        }

        lock.lock();
        Reader in = null;

        try
        {

            in = new FileReader( configurationFile );

            XStream xs = getXStream();
            Object config = xs.fromXML( in );

            if ( config != null )
            {
                this.configuration = (Configuration) config;
            }
            else
            {
                this.configuration = new Configuration();
            }

        }
        catch ( FileNotFoundException e )
        {
            this.configuration = new Configuration();
        }
        catch ( XStreamException e )
        {
            getLogger().error( "Invalid configuration XML", e );
        }
        finally
        {
            lock.unlock();

            if ( in != null )
            {
                try
                {
                    in.close();
                }
                catch ( IOException e )
                {
                    // just closing
                }
            }
        }

        return this.configuration;
    }

}
