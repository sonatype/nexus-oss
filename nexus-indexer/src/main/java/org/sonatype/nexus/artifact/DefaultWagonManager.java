/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.xml.Xpp3Dom;

@Component( role = WagonManager.class )
public class DefaultWagonManager
    extends AbstractLogEnabled
    implements WagonManager, Contextualizable
{
    private PlexusContainer container;

    /** Map( String, XmlPlexusConfiguration ) with the repository id and the wagon configuration */
    private Map<String, XmlPlexusConfiguration> serverConfigurationMap = new HashMap<String, XmlPlexusConfiguration>();

    @Requirement( role = Wagon.class )
    private Map<String, Wagon> wagons;

    private boolean interactive = true;

    public Wagon getWagon( Repository repository )
        throws WagonException
    {
        String protocol = repository.getProtocol();

        if ( protocol == null )
        {
            throw new UnsupportedProtocolException( "The repository " + repository + " does not specify a protocol" );
        }

        Wagon wagon = getWagon( protocol );

        configureWagon( wagon, repository.getId() );

        return wagon;
    }

    public Wagon getWagon( String protocol )
        throws WagonException
    {
        Wagon wagon = (Wagon) wagons.get( protocol );

        if ( wagon == null )
        {
            throw new UnsupportedProtocolException( "Cannot find wagon which supports the requested protocol: "
                + protocol );
        }

        wagon.setInteractive( interactive );

        return wagon;
    }

    public void setInteractive( boolean interactive )
    {
        this.interactive = interactive;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    private void configureWagon( Wagon wagon, String repositoryId )
        throws WagonConfigurationException
    {
        if ( serverConfigurationMap.containsKey( repositoryId ) )
        {
            ComponentConfigurator componentConfigurator = null;

            try
            {
                componentConfigurator = new BasicComponentConfigurator();

                componentConfigurator.configureComponent( wagon, serverConfigurationMap.get( repositoryId ), container
                    .getContainerRealm() );
            }
            catch ( ComponentConfigurationException e )
            {
                throw new WagonConfigurationException( "Unable to apply wagon configuration for repository: "
                    + repositoryId, e );
            }
            finally
            {
                if ( componentConfigurator != null )
                {
                    try
                    {
                        container.release( componentConfigurator );
                    }
                    catch ( ComponentLifecycleException e )
                    {
                        getLogger().error( "Problem releasing configurator - ignoring: " + e.getMessage() );
                    }
                }

            }
        }
    }

    public void addConfiguration( String repositoryId, Xpp3Dom configuration )
    {
        if ( ( repositoryId == null ) || ( configuration == null ) )
        {
            throw new IllegalArgumentException( "arguments can't be null" );
        }

        final XmlPlexusConfiguration xmlConf = new XmlPlexusConfiguration( configuration );

        serverConfigurationMap.put( repositoryId, xmlConf );
    }

    class WagonConfigurationException
        extends WagonException
    {
        private static final long serialVersionUID = -5257376525065802898L;

        public WagonConfigurationException( final String message )
        {
            super( message );
        }

        public WagonConfigurationException( final String message, final Throwable throwable )
        {
            super( message, throwable );
        }
    }
}
