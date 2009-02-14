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
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.StaticSecurityResource;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventUpdate;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

@Component( role = StaticSecurityResource.class, hint = "NexusStaticSecurityResource" )
public class NexusStaticSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource, EventListener, Initializable
{
    @Requirement
    RepositoryRegistry repoRegistry;

    @Requirement( role = ConfigurationManager.class, hint = "default" )
    ConfigurationManager configManager;

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
        return new Configuration();
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( RepositoryRegistryEventAdd.class.isAssignableFrom( evt.getClass() )
            || RepositoryRegistryEventUpdate.class.isAssignableFrom( evt.getClass() ) )
        {
            setDirty( true );
        }
        else if ( RepositoryRegistryEventRemove.class.isAssignableFrom( evt.getClass() ) )
        {
            setDirty( true );
            configManager.save();
        }
    }
}
