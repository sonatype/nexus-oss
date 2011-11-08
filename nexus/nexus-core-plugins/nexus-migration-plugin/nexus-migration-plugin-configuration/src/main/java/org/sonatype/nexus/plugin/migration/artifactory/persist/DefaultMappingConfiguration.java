/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
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

    private long lastModified;

    private XStream xstream;

    public void addMapping( CMapping map )
        throws IOException
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
        throws IOException
    {
        lock.lock();

        Configuration cfg = getConfiguration();

        FileOutputStream out = null;
        try
        {
            if ( !configurationFile.exists() )
            {
                configurationFile.getParentFile().mkdirs();
                configurationFile.createNewFile();
            }

            out = new FileOutputStream( configurationFile );

            getXStream().toXML( cfg, out );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to save mapping configuration", e );
            throw e;
        }
        finally
        {
            IOUtil.close( out );

            lock.unlock();
        }
    }

    private XStream getXStream()
    {
        if ( xstream == null )
        {
            xstream = new XStream();
            xstream.setClassLoader( getClass().getClassLoader() );
            xstream.processAnnotations( Configuration.class );
            xstream.processAnnotations( CMapping.class );
        }
        return xstream;
    }

    private Configuration getConfiguration()
    {
        if ( this.configuration != null && lastModified == configurationFile.lastModified() )
        {
            return this.configuration;
        }

        lock.lock();
        FileInputStream in = null;
        try
        {
            lastModified = configurationFile.lastModified();

            in = new FileInputStream( configurationFile );

            Object config = getXStream().fromXML( in );

            this.configuration = config != null
                ? (Configuration) config
                : new Configuration();
        }
        catch ( FileNotFoundException e )
        {
            this.configuration = new Configuration();
        }
        catch ( XStreamException e )
        {
            getLogger().error( "Invalid configuration XML", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Error reading configuration", e );
        }
        finally
        {
            IOUtil.close( in );

            lock.unlock();
        }

        return this.configuration;
    }

    public CMapping getMapping( String repositoryId )
    {
        if ( repositoryId == null )
        {
            return null;
        }

        List<CMapping> urls = getConfiguration().getUrlsMapping();
        for ( CMapping mapping : urls )
        {
            if ( repositoryId.equals( mapping.getArtifactoryRepositoryId() ) )
            {
                return mapping;
            }
        }

        return null;
    }

    public String getNexusContext()
    {
        return getConfiguration().getNexusContext();
    }

    public void setNexusContext( String nexusContext )
        throws IOException
    {
        lock.lock();
        try
        {
            getConfiguration().setNexusContext( nexusContext );

            save();
        }
        finally
        {
            lock.unlock();
        }
    }

    public List<CMapping> listMappings()
    {
        return getConfiguration().getUrlsMapping();
    }

}
