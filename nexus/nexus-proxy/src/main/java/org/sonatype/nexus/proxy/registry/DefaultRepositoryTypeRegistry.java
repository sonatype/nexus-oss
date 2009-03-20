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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;

@Component( role = RepositoryTypeRegistry.class )
public class DefaultRepositoryTypeRegistry
    extends AbstractLogEnabled
    implements RepositoryTypeRegistry
{
    @Requirement
    private PlexusContainer container;

    @Requirement( role = ContentClass.class )
    private Map<String, ContentClass> contentClasses;

    private Set<RepositoryTypeDescriptor> repositoryRoles;

    public Set<RepositoryTypeDescriptor> getRepositoryTypeDescriptors()
    {
        if ( repositoryRoles == null )
        {
            repositoryRoles = new HashSet<RepositoryTypeDescriptor>();

            // fill in the defaults
            repositoryRoles.add( new RepositoryTypeDescriptor( Repository.class.getName(), "repositories" ) );
            repositoryRoles.add( new RepositoryTypeDescriptor( ShadowRepository.class.getName(), "shadows" ) );
            repositoryRoles.add( new RepositoryTypeDescriptor( GroupRepository.class.getName(), "groups" ) );
            repositoryRoles.add( new RepositoryTypeDescriptor( WebSiteRepository.class.getName(), "sites" ) );
        }

        return repositoryRoles;
    }

    public Set<String> getRepositoryRoles()
    {
        Set<RepositoryTypeDescriptor> rtds = getRepositoryTypeDescriptors();

        HashSet<String> result = new HashSet<String>( rtds.size() );

        for ( RepositoryTypeDescriptor rtd : rtds )
        {
            result.add( rtd.getRole() );
        }

        return Collections.unmodifiableSet( result );
    }

    public Collection<ContentClass> getContentClasses()
    {
        return Collections.unmodifiableCollection( contentClasses.values() );
    }

    public Set<String> getExistingRepositoryHints( String role )
    {
        if ( !getRepositoryRoles().contains( role ) )
        {
            return Collections.emptySet();
        }

        List<ComponentDescriptor<Repository>> components = container
            .getComponentDescriptorList( Repository.class, role );

        HashSet<String> result = new HashSet<String>( components.size() );

        for ( ComponentDescriptor<Repository> component : components )
        {
            result.add( component.getRoleHint() );
        }

        return result;
    }

    public ContentClass getRepositoryContentClass( String role, String hint )
    {
        if ( !getRepositoryRoles().contains( role ) )
        {
            return null;
        }

        if ( container.hasComponent( Repository.class, role, hint ) )
        {
            ComponentDescriptor<Repository> descriptor = container
                .getComponentDescriptor( Repository.class, role, hint );

            String contentClassHint = null;

            for ( ComponentRequirement req : descriptor.getRequirements() )
            {
                if ( StringUtils.equals( ContentClass.class.getName(), req.getRole() ) )
                {
                    // XXX: shadow has two of these!
                    contentClassHint = req.getRoleHint();
                }
            }

            if ( contentClassHint != null )
            {
                return contentClasses.get( contentClassHint );
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public String getRepositoryDescription( String role, String hint )
    {
        if ( !getRepositoryRoles().contains( role ) )
        {
            return null;
        }

        if ( container.hasComponent( Repository.class, role, hint ) )
        {
            ComponentDescriptor<Repository> component = container.getComponentDescriptor( Repository.class, role, hint );

            if ( component != null ) // but we asked for it with hasComponent()?
            {
                if ( !StringUtils.isEmpty( component.getDescription() ) )
                {
                    return component.getDescription();
                }
                else
                {
                    return "";
                }
            }
            else
            {
                // component descriptor is null?
                return null;
            }
        }
        else
        {
            // component is not found
            return null;
        }
    }
}
