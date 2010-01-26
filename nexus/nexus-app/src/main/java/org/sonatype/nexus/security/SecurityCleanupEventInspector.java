package org.sonatype.nexus.security;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryTargetPropertyDescriptor;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.TargetRegistryEventRemove;
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
        return evt instanceof RepositoryRegistryEventRemove || evt instanceof TargetRegistryEventRemove;
    }

    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryRegistryEventRemove )
        {
            RepositoryRegistryEventRemove rEvt = (RepositoryRegistryEventRemove) evt;

            String repositoryId = rEvt.getRepository().getId();

            try
            {
                // Delete target privs that match repo/groupId
                cleanupPrivileges( TargetPrivilegeRepositoryPropertyDescriptor.ID, repositoryId );
                cleanupPrivileges( TargetPrivilegeGroupPropertyDescriptor.ID, repositoryId );
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
        if ( evt instanceof TargetRegistryEventRemove )
        {
            TargetRegistryEventRemove rEvt = (TargetRegistryEventRemove) evt;
            
            String targetId = rEvt.getTarget().getId();

            try
            {
                cleanupPrivileges( TargetPrivilegeRepositoryTargetPropertyDescriptor.ID, targetId );
            }
            catch ( NoSuchPrivilegeException e )
            {
                getLogger().error( "Unable to clean privileges attached to target: " + targetId, e );
            }
            catch ( NoSuchAuthorizationManager e )
            {
                getLogger().error( "Unable to clean privileges attached to target: " + targetId, e );
            }
        }
    }

    protected void cleanupPrivileges( String propertyId, String propertyValue )
        throws NoSuchPrivilegeException, NoSuchAuthorizationManager
    {
        Set<Privilege> privileges = security.listPrivileges();

        Set<String> removedIds = new HashSet<String>();

        for ( Privilege privilege : privileges )
        {
            if ( !privilege.isReadOnly() && privilege.getType().equals( TargetPrivilegeDescriptor.TYPE )
                && ( propertyValue.equals( privilege.getPrivilegeProperty( propertyId ) ) ) )
            {
                getLogger().debug( "Removing Privilege " + privilege.getName() + " because repository was removed" );
                security.getAuthorizationManager( SecurityXmlAuthorizationManager.SOURCE ).deletePrivilege(
                                                                                                            privilege.getId() );
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
