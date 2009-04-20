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
package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jsecurity.subject.Subject;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;

/**
 * A filter that maps the action from the HTTP Verb.
 * 
 * @author cstamas
 */
public class HttpVerbMappingAuthorizationFilter
    extends org.sonatype.jsecurity.web.filter.authz.HttpVerbMappingAuthorizationFilter
{

    private AuthcAuthzEvent currentAuthzEvt;
    
    protected Nexus getNexus()
    {
        return (Nexus) getAttribute( Nexus.class.getName() );
    }

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
        recordAuthzFailureEvent( request, response );

        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );

        return false;
    }

    private void recordAuthzFailureEvent( ServletRequest request, ServletResponse response )
    {
        Nexus nexus = getNexus();

        Subject subject = getSubject( request, response );

        if ( nexus.getAnonymousUsername().equals( subject.getPrincipal() ) )
        {
            return;
        }

        String msg = "Unable to authorize user [" + subject.getPrincipal() + "] for " + getActionFromHttpVerb( request )
            + " to " + ( (HttpServletRequest) request ).getRequestURI() + " from address/host ["
            + request.getRemoteAddr() + "/" + request.getRemoteHost() + "]";

        if ( isSimilarEvent( msg ) )
        {
            return;
        }
        
        getLogger().info( msg );

        getLogger().info( msg );

        AuthcAuthzEvent authzEvt = new AuthcAuthzEvent( FeedRecorder.SYSTEM_AUTHZ, msg );

        nexus.addAuthcAuthzEvent( authzEvt );

        currentAuthzEvt = authzEvt;
    }

    private boolean isSimilarEvent( String msg )
    {
        if ( currentAuthzEvt == null )
        {
            return false;
        }

        if ( currentAuthzEvt.getMessage().equals( msg )
            && ( System.currentTimeMillis() - currentAuthzEvt.getEventDate().getTime() < 2000L ) )
        {
            return true;
        }

        return false;
    }

}
