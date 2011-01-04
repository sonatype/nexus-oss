/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.security;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryTargetPropertyDescriptor;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.TargetRegistryEventRemove;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.xml.SecurityXmlAuthorizationManager;
import org.sonatype.security.realms.tools.ConfigurationManager;

@Component( role = EventInspector.class, hint = "SecurityCleanupEventInspector" )
public class SecurityCleanupEventInspector
    extends AbstractEventInspector
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
            catch ( NoSuchAuthorizationManagerException e )
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
            catch ( NoSuchAuthorizationManagerException e )
            {
                getLogger().error( "Unable to clean privileges attached to target: " + targetId, e );
            }
        }
    }

    protected void cleanupPrivileges( String propertyId, String propertyValue )
        throws NoSuchPrivilegeException, NoSuchAuthorizationManagerException
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
