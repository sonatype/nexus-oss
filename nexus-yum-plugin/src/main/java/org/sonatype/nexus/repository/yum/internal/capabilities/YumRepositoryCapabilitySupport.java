/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.capabilities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import javax.inject.Inject;

import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.support.condition.Conditions;
import org.sonatype.nexus.plugins.capabilities.support.condition.RepositoryConditions;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.repository.yum.YumRegistry;
import com.google.common.base.Throwables;

public abstract class YumRepositoryCapabilitySupport<C extends YumRepositoryCapabilityConfigurationSupport>
    extends CapabilitySupport
{

    private final YumRegistry yumRegistry;

    private final Conditions conditions;

    private final RepositoryRegistry repositoryRegistry;

    private C configuration;

    @Inject
    public YumRepositoryCapabilitySupport( final YumRegistry yumRegistry,
                                           final Conditions conditions,
                                           final RepositoryRegistry repositoryRegistry )
    {
        this.yumRegistry = checkNotNull( yumRegistry );
        this.conditions = checkNotNull( conditions );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
    }

    @Override
    public String description()
    {
        if ( configuration != null )
        {
            try
            {
                return repositoryRegistry.getRepository( configuration.repository() ).getName();
            }
            catch ( NoSuchRepositoryException e )
            {
                return configuration.repository();
            }
        }
        return null;
    }

    @Override
    public void onCreate()
        throws Exception
    {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onLoad()
        throws Exception
    {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onUpdate()
        throws Exception
    {
        configuration = createConfiguration( context().properties() );
        final Yum yum = yumRegistry.get( configuration.repository() );
        // yum is not present when repository is changed
        if ( yum != null )
        {
            configureYum( yum );
        }
    }

    @Override
    public void onRemove()
        throws Exception
    {
        configuration = null;
    }

    @Override
    public void onActivate()
    {
        try
        {
            final Repository repository = repositoryRegistry.getRepository( configuration.repository() );
            configureYum( yumRegistry.register( repository.adaptToFacet( MavenRepository.class ) ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            // TODO
            throw Throwables.propagate( e );
        }
    }

    @Override
    public void onPassivate()
    {
        yumRegistry.unregister( configuration.repository() );
    }

    @Override
    public Condition activationCondition()
    {
        return conditions.logical().and(
            conditions.capabilities().capabilityOfTypeActive( YumCapabilityDescriptor.TYPE ),
            conditions.repository().repositoryIsInService( new RepositoryConditions.RepositoryId()
            {
                @Override
                public String get()
                {
                    return isConfigured() ? configuration.repository() : null;
                }
            } ),
            conditions.capabilities().passivateCapabilityWhenPropertyChanged(
                YumRepositoryCapabilityConfigurationSupport.REPOSITORY_ID
            )
        );
    }

    @Override
    public Condition validityCondition()
    {
        return conditions.repository().repositoryExists( new RepositoryConditions.RepositoryId()
        {
            @Override
            public String get()
            {
                return isConfigured() ? configuration.repository() : null;
            }
        } );
    }

    public C configuration()
    {
        return configuration;
    }

    void configureYum( final Yum yum )
    {
        // template method
    }

    abstract C createConfiguration( final Map<String, String> properties );

    boolean isConfigured()
    {
        return configuration != null;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName()
            + ( isConfigured() ? "{repository=" + configuration.repository() + "}" : "" );
    }

}
