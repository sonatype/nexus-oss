package org.sonatype.nexus.security;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.xml.SecurityXmlAuthorizationManager;
import org.sonatype.security.realms.tools.ConfigurationManager;

@Component( role = EventInspector.class, hint = "SecurityCleanupEventInspector" )
public class SecurityCleanupEventInspector
    extends AbstractLogEnabled
    implements EventInspector
{
    @Requirement( hint = "default" )
    private ConfigurationManager configManager;
    
    @Requirement
    private SecuritySystem security;
    
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryRegistryEventRemove;
    }
    
    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryRegistryEventRemove )
        {
            RepositoryRegistryEventRemove rEvt = ( RepositoryRegistryEventRemove ) evt;
            
            String repositoryId = rEvt.getRepository().getId();
            
            try
            {
                cleanupPrivileges( repositoryId );
            }
            catch ( NoSuchPrivilegeException e )
            {
                getLogger().error( "Unable to clean privileges attached to repository", e );
            }
            catch ( NoSuchAuthorizationManager e )
            {
                getLogger().error( "Unable to clean privileges attached to repository", e );
            }
        }
    }
    
    protected void cleanupPrivileges( String repositoryId ) 
        throws NoSuchPrivilegeException, 
            NoSuchAuthorizationManager
    {
        Set<Privilege> privileges = security.listPrivileges();
        
        Set<String> removedIds = new HashSet<String>();
        
        for ( Privilege privilege : privileges )
        {
            // Delete target privs that match repo/groupId
            if ( privilege.getType().equals( TargetPrivilegeDescriptor.TYPE ) 
                && ( repositoryId.equals( privilege.getPrivilegeProperty( TargetPrivilegeRepositoryPropertyDescriptor.ID ) ) 
                || repositoryId.equals( privilege.getPrivilegeProperty( TargetPrivilegeGroupPropertyDescriptor.ID ) ) ) )
            {
                getLogger().debug( "Removing Privilege " + privilege.getName() + " because repository was removed" );
                security.getAuthorizationManager( SecurityXmlAuthorizationManager.SOURCE ).deletePrivilege( privilege.getId() );
                removedIds.add( privilege.getId() );
            }
        }        
        
        for ( String privilegeId : removedIds )
        {
            configManager.cleanRemovedPrivilege( privilegeId );
        }        
        configManager.save();
    }
}
