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
package org.sonatype.jsecurity.web.filter.authz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.web.filter.authz.PermissionsAuthorizationFilter;

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
        mapping.put( "mkcol", "create" );
    }

    protected Log getLogger()
    {
        return logger;
    }

    protected String getActionFromHttpVerb( String method )
    {
        method = method.toLowerCase();

        if ( mapping.containsKey( method ) )
        {
            method = mapping.get( method );
        }

        return method;
    }

    protected String getActionFromHttpVerb( ServletRequest request )
    {
        String action = ( (HttpServletRequest) request ).getMethod();

        return getActionFromHttpVerb( action );
    }

    protected String[] mapPerms( String[] perms, String action )
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

        if ( super.isAccessAllowed( request, response, mapPerms( perms, getActionFromHttpVerb( request ) ) ) )
        {
            return true;
        }

        return false;
    }

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
//        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );

        return super.onAccessDenied( request, response );
    }    
}
