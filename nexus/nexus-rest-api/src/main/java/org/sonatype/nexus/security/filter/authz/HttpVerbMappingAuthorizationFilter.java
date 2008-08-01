package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.web.filter.authz.PermissionsAuthorizationFilter;
import org.sonatype.nexus.Nexus;
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

    protected String getActionFromHttpVerb( HttpServletRequest request )
    {
        String action = request.getMethod().toLowerCase();

        if ( mapping.containsKey( action ) )
        {
            action = mapping.get( action );
        }

        return action;
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

    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        String action = getActionFromHttpVerb( (HttpServletRequest) request );

        String[] perms = (String[]) mappedValue;

        return super.isAccessAllowed( request, response, mapPerms( perms, action ) );
    }

    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );

        return false;
    }

}
