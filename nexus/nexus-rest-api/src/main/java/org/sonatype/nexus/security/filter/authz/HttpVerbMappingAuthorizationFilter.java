package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.web.filter.authz.PermissionsAuthorizationFilter;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;

/**
 * A filter that maps the action from the HTTP Verb.
 * 
 * @author cstamas
 */
public class HttpVerbMappingAuthorizationFilter
    extends PermissionsAuthorizationFilter
{
    private Map<String, String> mapping = new HashMap<String, String>();
    {
        mapping.put( "get", "read" );
        mapping.put( "put", "update" );
        mapping.put( "post", "create" );
    }

    protected String getActionFromHttpVerb( HttpServletRequest request )
    {
        String method = request.getMethod().toLowerCase();

        if ( mapping.containsKey( method ) )
        {
            return mapping.get( method );
        }
        else
        {
            return method;
        }
    }

    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        String action = getActionFromHttpVerb( (HttpServletRequest) request );

        String[] perms = (String[]) mappedValue;

        if ( perms != null && perms.length > 0 )
        {

            String[] mappedPerms = new String[perms.length];

            for ( int i = 0; i < perms.length; i++ )
            {
                mappedPerms[i] = perms[i] + ":" + action;
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "Mapped permissions to '" + StringUtils.join( mappedPerms, ", " ) + "'" );
            }

            return super.isAccessAllowed( request, response, mappedPerms );
        }
        else
        {
            return super.isAccessAllowed( request, response, mappedValue );
        }
    }

    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );

        return false;
    }

}
