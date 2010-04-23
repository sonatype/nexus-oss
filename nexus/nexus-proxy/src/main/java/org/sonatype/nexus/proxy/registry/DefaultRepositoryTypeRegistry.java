/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.maven.maven1.M1GroupRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Component( role = RepositoryTypeRegistry.class )
public class DefaultRepositoryTypeRegistry
    implements RepositoryTypeRegistry
{
    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer container;

    @Requirement( role = ContentClass.class )
    private Map<String, ContentClass> contentClasses;

    private Map<String, ContentClass> repoCachedContentClasses = new HashMap<String, ContentClass>();

    private Multimap<String, RepositoryTypeDescriptor> repositoryTypeDescriptorsMap;

    protected Logger getLogger()
    {
        return logger;
    }

    protected Multimap<String, RepositoryTypeDescriptor> getRepositoryTypeDescriptors()
    {
        if ( repositoryTypeDescriptorsMap == null )
        {
            synchronized ( this )
            {
                // maybe the previous who was blocking us already did the job
                if ( repositoryTypeDescriptorsMap == null )
                {
                    Multimap<String, RepositoryTypeDescriptor> result = Multimaps.newArrayListMultimap();

                    // fill in the defaults
                    String role = null;

                    role = Repository.class.getName();

                    result.put( role, new RepositoryTypeDescriptor( role, M1Repository.ID, "repositories",
                        RepositoryType.UNLIMITED_INSTANCES ) );
                    result.put( role, new RepositoryTypeDescriptor( role, M2Repository.ID, "repositories",
                        RepositoryType.UNLIMITED_INSTANCES ) );

                    role = ShadowRepository.class.getName();

                    result.put( role, new RepositoryTypeDescriptor( role, M1LayoutedM2ShadowRepository.ID, "shadows",
                        RepositoryType.UNLIMITED_INSTANCES ) );
                    result.put( role, new RepositoryTypeDescriptor( role, M2LayoutedM1ShadowRepository.ID, "shadows",
                        RepositoryType.UNLIMITED_INSTANCES ) );

                    role = GroupRepository.class.getName();

                    result.put( role, new RepositoryTypeDescriptor( role, M1GroupRepository.ID, "groups",
                        RepositoryType.UNLIMITED_INSTANCES ) );
                    result.put( role, new RepositoryTypeDescriptor( role, M2GroupRepository.ID, "groups",
                        RepositoryType.UNLIMITED_INSTANCES ) );

                    // No implementation exists in core!
                    // role = WebSiteRepository.class.getName();

                    // result.put( role, new RepositoryTypeDescriptor( role, XXX, "sites" ) );

                    getLogger().info( "Registered default repository types." );

                    this.repositoryTypeDescriptorsMap = result;
                }
            }
        }

        return repositoryTypeDescriptorsMap;
    }

    public Set<RepositoryTypeDescriptor> getRegisteredRepositoryTypeDescriptors()
    {
        return Collections.unmodifiableSet( new HashSet<RepositoryTypeDescriptor>(
            getRepositoryTypeDescriptors().values() ) );
    }

    public boolean registerRepositoryTypeDescriptors( RepositoryTypeDescriptor d )
    {
        if ( d.getRepositoryMaxInstanceCount() < 0 )
        {
            getLogger().info( "Registered Repository type " + d.toString() + "." );
        }
        else
        {
            getLogger().info(
                "Registered Repository type " + d.toString() + " with maximal instance limit set to "
                    + d.getRepositoryMaxInstanceCount() + "." );
        }

        return getRepositoryTypeDescriptors().put( d.getRole(), d );
    }

    public boolean unregisterRepositoryTypeDescriptors( RepositoryTypeDescriptor d )
    {
        getLogger().info( "Unregistered repository type " + d.toString() );

        return getRepositoryTypeDescriptors().remove( d.getRole(), d );
    }

    public Map<String, ContentClass> getContentClasses()
    {
        return Collections.unmodifiableMap( new HashMap<String, ContentClass>( contentClasses ) );
    }

    public Set<String> getRepositoryRoles()
    {
        Set<RepositoryTypeDescriptor> rtds = getRegisteredRepositoryTypeDescriptors();

        HashSet<String> result = new HashSet<String>( rtds.size() );

        for ( RepositoryTypeDescriptor rtd : rtds )
        {
            result.add( rtd.getRole() );
        }

        return Collections.unmodifiableSet( result );
    }

    public Set<String> getExistingRepositoryHints( String role )
    {
        if ( !getRepositoryTypeDescriptors().containsKey( role ) )
        {
            return Collections.emptySet();
        }

        HashSet<String> result = new HashSet<String>();

        for ( RepositoryTypeDescriptor rtd : getRepositoryTypeDescriptors().get( role ) )
        {
            result.add( rtd.getHint() );
        }

        return result;
    }

    public RepositoryTypeDescriptor getRepositoryTypeDescriptor( String role, String hint )
    {
        if ( !getRepositoryTypeDescriptors().containsKey( role ) )
        {
            return null;
        }

        for ( RepositoryTypeDescriptor rtd : getRepositoryTypeDescriptors().get( role ) )
        {
            if ( rtd.getHint().equals( hint ) )
            {
                return rtd;
            }
        }

        return null;
    }

    // TODO: solve this single place to cease the requirement for PlexusContainer!
    public ContentClass getRepositoryContentClass( String role, String hint )
    {
        if ( !getRepositoryRoles().contains( role ) )
        {
            return null;
        }

        ContentClass result = null;

        String cacheKey = role + ":" + hint;

        if ( repoCachedContentClasses.containsKey( cacheKey ) )
        {
            result = repoCachedContentClasses.get( cacheKey );
        }
        else
        {
            if ( container.hasComponent( Repository.class, role, hint ) )
            {
                try
                {
                    Repository repository = container.lookup( Repository.class, role, hint );

                    result = repository.getRepositoryContentClass();

                    container.release( repository );

                    repoCachedContentClasses.put( cacheKey, result );
                }
                catch ( ComponentLookupException e )
                {
                    getLogger().warn( "Container contains a component but lookup failed!", e );
                }
                catch ( ComponentLifecycleException e )
                {
                    getLogger().warn( "Could not release the component! Possible leak here.", e );
                }
            }
            else
            {
                return null;
            }
        }

        return result;
    }
}
