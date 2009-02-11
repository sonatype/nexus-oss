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
package org.sonatype.nexus.jsecurity;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.jsecurity.realms.tools.StaticSecurityResource;
import org.sonatype.nexus.jsecurity.realms.RepositoryPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.RepositoryViewPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventUpdate;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = StaticSecurityResource.class, hint = "NexusStaticSecurityResource" )
public class NexusStaticSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource,
        EventListener,
        Initializable
{    
    @Requirement
    RepositoryRegistry repoRegistry;
    
    public void initialize()
        throws InitializationException
    {
        repoRegistry.addProximityEventListener( this );
    }
    
    public String getResourcePath()
    {
        return "/META-INF/nexus/static-security.xml";
    }
    
    public Configuration getConfiguration()
    {
        Configuration configuration = new Configuration();
        
        configuration.addPrivilege( buildPrivilege(
            "Repository (All) - (view)",
            "Privilege that gives view access to all repositories.",
            "*" ) );
        
        for ( Repository repo : repoRegistry.getRepositories() )
        {
            configuration.addPrivilege( buildPrivilege(
                "Repository (" + repo.getName() + ") - (view)",
                "Privilege that gives view access to the " + repo.getName() + " repository.",
                repo.getId() ) );
        }
        
            
        setDirty( false );
        
        return configuration;
    }
    
    protected CPrivilege buildPrivilege( String name, String description, String repoId )
    {
        CPrivilege priv = new CPrivilege();
        
        priv.setId( "repository-" + ( repoId.equals( "*" ) ? "all" : repoId ) );
        priv.setName( name );
        priv.setDescription( description );
        priv.setType( RepositoryViewPrivilegeDescriptor.TYPE );
        
        CProperty prop = new CProperty();
        prop.setKey( RepositoryPropertyDescriptor.ID );
        prop.setValue( repoId );        
        priv.addProperty( prop );
        
        return priv;
    }
    
    public void onProximityEvent( AbstractEvent evt )
    {
        if ( RepositoryRegistryEventAdd.class.isAssignableFrom( evt.getClass() ) 
            || RepositoryRegistryEventUpdate.class.isAssignableFrom( evt.getClass() ) 
            || RepositoryRegistryEventRemove.class.isAssignableFrom( evt.getClass() ) )
        {
            setDirty( true );
        }
    }
}
