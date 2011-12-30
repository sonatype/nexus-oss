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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableCollection;
import static org.sonatype.nexus.plugins.capabilities.api.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.api.CapabilityType.capabilityType;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.ConfigurationIdGenerator;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Validator;
import org.sonatype.nexus.plugins.capabilities.api.ValidatorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Reader;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Writer;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

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

    private final ValidatorRegistry validatorRegistry;

    private final File configurationFile;

    private final ReentrantLock lock = new ReentrantLock();

    private Configuration configuration;

    @Inject
    public DefaultCapabilityConfiguration( final ApplicationConfiguration applicationConfiguration,
                                           final NexusEventBus eventBus,
                                           final CapabilityConfigurationValidator validator,
                                           final ConfigurationIdGenerator idGenerator,
                                           final ValidatorRegistry validatorRegistry )
    {
        this.eventBus = eventBus;
        this.validator = validator;
        this.idGenerator = idGenerator;
        this.validatorRegistry = checkNotNull( validatorRegistry );

        configurationFile = new File( applicationConfiguration.getWorkingDirectory(), "conf/capabilities.xml" );
    }

    public String add( final CCapability capability )
        throws InvalidConfigurationException, IOException
    {
        lock.lock();

        try
        {
            {
                final ValidationResponse vr = validator.validate( capability, true );

                if ( vr.getValidationErrors().size() > 0 )
                {
                    throw new InvalidConfigurationException( vr );
                }
            }

            validate(
                validatorRegistry.get( capabilityType( capability.getTypeId() ) ),
                asMap( capability.getProperties() )
            );

            final String generatedId = idGenerator.generateId();

            capability.setId( generatedId );
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

    private void validate( final Collection<Validator> validators, final Map<String, String> properties )
        throws InvalidConfigurationException
    {
        if ( validators != null && !validators.isEmpty())
        {
            final ValidationResponse vr = new ValidationResponse();

            for ( final Validator validator : validators )
            {
                final Set<Validator.Violation> violations = validator.validate( properties );
                if ( violations != null && !violations.isEmpty() )
                {
                    for ( final Validator.Violation violation : violations )
                    {
                        vr.addValidationError( new ValidationMessage(
                            violation.property() == null ? "*" : violation.property(),
                            violation.message()
                        ) );
                    }
                }
            }

            if ( vr.getValidationErrors().size() > 0 )
            {
                throw new InvalidConfigurationException( vr );
            }
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

            validate(
                validatorRegistry.get( capabilityIdentity( capability.getId() ) ),
                asMap( capability.getProperties() )
            );

            final CCapability stored = getInternal( capability.getId() );

            if ( stored != null )
            {
                getConfiguration().removeCapability( stored );
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
            final CCapability stored = getInternal( capabilityId );
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
        final CCapability capability = getInternal( capabilityId );
        if ( capability != null )
        {
            return capability.clone();
        }
        return null;
    }

    public Collection<CCapability> getAll()
        throws InvalidConfigurationException, IOException
    {
        return unmodifiableCollection( clone( getConfiguration().getCapabilities() ) );
    }

    @Override
    public Collection<CCapability> get( final Predicate<CCapability> filter )
        throws InvalidConfigurationException, IOException
    {
        return unmodifiableCollection( clone( Collections2.filter( getAll(), filter ) ) );
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

            Xpp3DomBuilder.build( r );

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
        eventBus.post( new CapabilitiesConfigurationEvent.AfterLoad( this ) );
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

    public CCapability getInternal( final String capabilityId )
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

    private Collection<CCapability> clone( final Collection<CCapability> capabilities )
    {
        if ( capabilities == null )
        {
            return null;
        }
        final List<CCapability> clones = Lists.newArrayList();
        for ( final CCapability capability : capabilities )
        {
            clones.add( capability.clone() );
        }
        return clones;
    }

}
