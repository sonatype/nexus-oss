/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.ConfigurationIdGenerator;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Reader;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Writer;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Handles persistence of capabilities configuration.
 */
@Singleton
@Named
public class DefaultCapabilityConfiguration
    extends AbstractLoggingComponent
    implements CapabilityConfiguration
{

    private final NexusEventBus eventBus;

    private final CapabilityConfigurationValidator validator;

    private final ConfigurationIdGenerator idGenerator;

    private final CapabilityDescriptorRegistry descriptors;

    private final File configurationFile;

    private final ReentrantLock lock = new ReentrantLock();

    private Configuration configuration;

    @Inject
    public DefaultCapabilityConfiguration( final ApplicationConfiguration applicationConfiguration,
                                           final NexusEventBus eventBus,
                                           final CapabilityConfigurationValidator validator,
                                           final ConfigurationIdGenerator idGenerator,
                                           final CapabilityDescriptorRegistry descriptors )
    {
        this.eventBus = eventBus;
        this.validator = validator;
        this.idGenerator = idGenerator;
        this.descriptors = descriptors;

        configurationFile = new File( applicationConfiguration.getWorkingDirectory(), "conf/capabilities.xml" );
    }

    public String add( final CCapability capability )
        throws InvalidConfigurationException, IOException
    {
        lock.lock();

        try
        {
            final ValidationResponse vr = validator.validate( capability, true );

            if ( vr.getValidationErrors().size() > 0 )
            {
                throw new InvalidConfigurationException( vr );
            }

            final String generatedId = idGenerator.generateId();

            capability.setId( generatedId );
            capability.setDescription( getDescription( capability ) );
            getConfiguration().addCapability( capability );

            save();

            getLogger().debug(
                "Added capability '{}' of type '{}' with properties '{}'",
                new Object[]{ capability.getId(), capability.getTypeId(), capability.getProperties() }
            );

            eventBus.post( new CapabilityConfigurationEvent.Added( capability ) );

            return generatedId;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void update( final CCapability capability )
        throws InvalidConfigurationException, IOException
    {
        lock.lock();

        try
        {
            final ValidationResponse vr = validator.validate( capability, false );

            if ( vr.getValidationErrors().size() > 0 )
            {
                throw new InvalidConfigurationException( vr );
            }

            final CCapability stored = get( capability.getId() );

            if ( stored != null )
            {
                getConfiguration().removeCapability( stored );
                capability.setDescription( getDescription( capability ) );
                getConfiguration().addCapability( capability );
                save();

                getLogger().debug(
                    "Updated capability '{}' of type '{}' with properties '{}'",
                    new Object[]{ capability.getId(), capability.getTypeId(), capability.getProperties() }
                );

                eventBus.post( new CapabilityConfigurationEvent.Updated( capability, stored ) );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void remove( final String capabilityId )
        throws InvalidConfigurationException, IOException
    {
        lock.lock();

        try
        {
            final CCapability stored = get( capabilityId );
            if ( stored != null )
            {
                getConfiguration().removeCapability( stored );
                save();

                getLogger().debug(
                    "Removed capability '{}' of type '{}' with properties '{}'",
                    new Object[]{ stored.getId(), stored.getTypeId(), stored.getProperties() }
                );
                eventBus.post( new CapabilityConfigurationEvent.Removed( stored ) );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public CCapability get( final String capabilityId )
        throws InvalidConfigurationException, IOException
    {
        if ( StringUtils.isEmpty( capabilityId ) )
        {
            return null;
        }

        for ( final CCapability capability : getConfiguration().getCapabilities() )
        {
            if ( capabilityId.equals( capability.getId() ) )
            {
                return capability;
            }
        }

        return null;
    }

    public Collection<CCapability> getAll()
        throws InvalidConfigurationException, IOException
    {
        return Collections.unmodifiableList( getConfiguration().getCapabilities() );
    }

    private Configuration getConfiguration()
        throws InvalidConfigurationException,
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
            final Reader r = new FileReader( configurationFile );

            final Xpp3Dom dom = Xpp3DomBuilder.build( r );

            is = new FileInputStream( configurationFile );

            final NexusCapabilitiesConfigurationXpp3Reader reader = new NexusCapabilitiesConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );

            final ValidationResponse vr = validator.validateModel(
                new ValidationRequest<Configuration>( configuration )
            );

            if ( vr.getValidationErrors().size() > 0 )
            {
                throw new InvalidConfigurationException( vr );
            }
        }
        catch ( final FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            configuration = new Configuration();

            configuration.setVersion( Configuration.MODEL_VERSION );

            save();
        }
        catch ( final IOException e )
        {
            getLogger().error( "IOException while retrieving configuration file", e );
        }
        catch ( final XmlPullParserException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        finally
        {
            IOUtil.close( fr );
            IOUtil.close( is );

            lock.unlock();
        }

        return configuration;
    }

    public void load()
        throws InvalidConfigurationException, IOException
    {
        final Collection<CCapability> capabilities = getAll();
        for ( final CCapability capability : capabilities )
        {
            getLogger().debug(
                "Loading capability '{}' of type '{}' with properties '{}'",
                new Object[]{ capability.getId(), capability.getTypeId(), capability.getProperties() }
            );
            eventBus.post( new CapabilityConfigurationEvent.Loaded( capability ) );
        }
    }

    public void save()
        throws IOException
    {
        lock.lock();

        configurationFile.getParentFile().mkdirs();

        Writer fw = null;

        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( configurationFile ) );

            final NexusCapabilitiesConfigurationXpp3Writer writer = new NexusCapabilitiesConfigurationXpp3Writer();

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
                catch ( final IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }
    }

    public void clearCache()
    {
        configuration = null;
    }

    private String getDescription( final CCapability capability )
    {
        final CapabilityDescriptor descriptor = descriptors.get( capability.getTypeId() );
        if ( descriptor != null )
        {
            try
            {
                return descriptor.describe( asMap( capability.getProperties() ) );
            }
            catch ( Exception ignore )
            {
                getLogger().warn( "Capability descriptor '{}' failed to describe capability", descriptor.id() );
            }
        }
        return capability.getDescription();
    }

    static Map<String, String> asMap( final List<CCapabilityProperty> properties )
    {
        final Map<String, String> map = new HashMap<String, String>();
        if ( properties != null )
        {
            for ( final CCapabilityProperty property : properties )
            {
                map.put( property.getKey(), property.getValue() );
            }
        }
        return map;
    }

}
