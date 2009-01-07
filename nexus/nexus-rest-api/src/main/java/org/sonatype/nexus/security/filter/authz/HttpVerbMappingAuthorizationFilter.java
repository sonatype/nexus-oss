/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.filter.authz.PermissionsAuthorizationFilter;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;

/**
 * A filter that maps the action from the HTTP Verb.
 * 
 * @author cstamas
 */
public class HttpVerbMappingAuthorizationFilter
    extends PermissionsAuthorizationFilter
{
    private final Log logger = LogFactory.getLog( this.getClass() );

    private Map<String, String> mapping = new HashMap<String, String>();
    {
        mapping.put( "head", "read" );
        mapping.put( "get", "read" );
        mapping.put( "put", "update" );
        mapping.put( "post", "create" );
    }

    protected Log getLogger()
    {
        return logger;
    }

    protected Nexus getNexus( ServletRequest request )
    {
        return (Nexus) request.getAttribute( Nexus.class.getName() );
    }

    protected Action getActionFromHttpVerb( String method )
    {
        method = method.toLowerCase();

        if ( mapping.containsKey( method ) )
        {
            method = mapping.get( method );
        }

        return Action.valueOf( method );
    }

    protected Action getActionFromHttpVerb( ServletRequest request )
    {
        String action = ( (HttpServletRequest) request ).getMethod();

        return getActionFromHttpVerb( action );
    }

    protected String[] mapPerms( String[] perms, Action action )
    {
        if ( perms != null && perms.length > 0 && action != null )
        {
            String[] mappedPerms = new String[perms.length];

            for ( int i = 0; i < perms.length; i++ )
            {
                mappedPerms[i] = perms[i] + ":" + action;
            }

            if ( getLogger().isDebugEnabled() )
            {
                StringBuffer sb = new StringBuffer();

                for ( int i = 0; i < mappedPerms.length; i++ )
                {
                    sb.append( mappedPerms[i] );

                    sb.append( ", " );
                }

                getLogger().debug(
                    "MAPPED '" + action + "' action to permission: " + sb.toString().substring( 0, sb.length() - 2 ) );
            }

            return mappedPerms;
        }
        else
        {
            return perms;
        }
    }

    @Override
    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        String[] perms = (String[]) mappedValue;

        return super.isAccessAllowed( request, response, mapPerms( perms, getActionFromHttpVerb( request ) ) );
    }

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
        Subject subject = getSubject( request, response );

        String msg = "Unable to authorize user [" + subject.getPrincipal() + "] for " + getActionFromHttpVerb( request )
            + " to " + ( (HttpServletRequest) request ).getRequestURI() + " from address/host ["
            + request.getRemoteAddr() + "/" + request.getRemoteHost() + "]";

        AuthcAuthzEvent aaEvt = new AuthcAuthzEvent(FeedRecorder.SYSTEM_AUTHZ, msg);
        
        getNexus( request ).addAuthcAuthzEvent( aaEvt );
        
        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );
        
        return false;
    }

}
