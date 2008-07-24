/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.session;

import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.util.StringDigester;

/**
 * A simple implementation of SessionStore using in memory ConcurrentHashMap.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultSessionStore
    extends AbstractLogEnabled
    implements SessionStore, Initializable, ConfigurationChangeListener
{
    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /**
     * Session maps.
     */
    private ConcurrentHashMap<String, Session> sessions;

    /**
     * Session expiration in ms.
     */
    private int sessionExpiration = 0;

    public DefaultSessionStore()
    {
        super();

        sessions = new ConcurrentHashMap<String, Session>();
    }

    public void initialize()
        throws InitializationException
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        sessionExpiration = 0;
    }

    public String addSession( Session session )
    {
        String id = StringDigester.getSha1Digest( session.getUser().getUsername()
            + Long.toString( System.currentTimeMillis() ) );

        sessions.put( id, session );

        getLogger().info( "User " + session.getUser().getUsername() + " created a session, ID=" + id );

        return id;
    }

    public Session getSession( String sessionId )
    {
        Session session = sessions.get( sessionId );

        if ( session != null )
        {
            if ( getSesionExpiration() != -1 )
            {
                if ( ( System.currentTimeMillis() - session.getCreated() ) > getSesionExpiration() )
                {
                    getLogger().info( "Session with ID=" + sessionId + " has expired." );

                    removeSession( sessionId );

                    session = null;
                }
            }
        }

        return session;
    }

    public void removeSession( String sessionId )
    {
        getLogger().info( "Removing session with ID=" + sessionId );

        sessions.remove( sessionId );
    }

    public int getSesionExpiration()
    {
        if ( sessionExpiration == 0 )
        {
            // setSessionExpiration( applicationConfiguration.getConfiguration().getRestApi().getAuthTokenExpiration()
            // );
            setSessionExpiration( 10000 );
        }
        return sessionExpiration;
    }

    public void setSessionExpiration( int seconds )
    {
        this.sessionExpiration = seconds * 60 * 1000;
    }

}
