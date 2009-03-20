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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.security.DefaultPlexusSecurity;

@Component( role = NexusSecurity.class )
public class DefaultNexusSecurity
    extends DefaultPlexusSecurity
    implements NexusSecurity
{
    @Requirement
    private PrivilegeInheritanceManager privInheritance;

    private List<EventListener> listeners = new ArrayList<EventListener>();

    @Override
    public void save()
    {
        super.save();

        // TODO: can we do the same here as with nexus config?
        notifyProximityEventListeners( new ConfigurationChangeEvent( this, null ) );
    }

    @Override
    public void createPrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException
    {
        addInheritedPrivileges( privilege );
        super.createPrivilege( privilege, context );
    }

    public void addProximityEventListener( EventListener listener )
    {
        listeners.add( listener );
    }

    public void removeProximityEventListener( EventListener listener )
    {
        listeners.remove( listener );
    }

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        for ( EventListener l : listeners )
        {
            try
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Notifying component about security config change: " + l.getClass().getName() );
                }

                l.onProximityEvent( evt );
            }
            catch ( Exception e )
            {
                getLogger().info( "Unexpected exception in listener", e );
            }
        }
    }

    private void addInheritedPrivileges( SecurityPrivilege privilege )
    {
        CProperty methodProperty = null;

        for ( CProperty property : (List<CProperty>) privilege.getProperties() )
        {
            if ( property.getKey().equals( "method" ) )
            {
                methodProperty = property;
                break;
            }
        }

        if ( methodProperty != null )
        {
            List<String> inheritedMethods = privInheritance.getInheritedMethods( methodProperty.getValue() );

            StringBuffer buf = new StringBuffer();

            for ( String method : inheritedMethods )
            {
                buf.append( method );
                buf.append( "," );
            }

            if ( buf.length() > 0 )
            {
                buf.setLength( buf.length() - 1 );

                methodProperty.setValue( buf.toString() );
            }
        }
    }

}
