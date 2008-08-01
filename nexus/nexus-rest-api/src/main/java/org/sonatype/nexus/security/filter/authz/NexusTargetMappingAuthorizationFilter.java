package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.web.WebUtils;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * A filter that maps the targetId from the Request.
 * 
 * @author cstamas
 */
public class NexusTargetMappingAuthorizationFilter
    extends HttpVerbMappingAuthorizationFilter
{
    private static final String[] FAKE_PERMS = new String[] { "nexus:admin" };

    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        // we have no mappedValue here! it is simply neglected, and we are building our own
        // perms based on request
        // we are building based on request path
        // permissions that are of form:
        // targetId : repoId
        // (and the superclass will append the action based on HTTP verb)
        // TODO: BAD, a lot of hardoced stuff!
        String path = StringUtils.stripStart(
            WebUtils.getPathWithinApplication( (HttpServletRequest) request ),
            "/content" );

        ResourceStoreRequest rsr = new ResourceStoreRequest( path, true );

        TargetSet matched = null;

        String[] result = null;

        try
        {
            matched = getNexus( request ).getRootRouter().getTargetsForRequest( rsr );

            List<String> perms = new ArrayList<String>( matched.size() );

            // targetId : repoId
            for ( TargetMatch match : matched )
            {
                perms.add( match.getTarget().getId() + ":" + match.getRepository().getId() );
            }

            result = perms.toArray( new String[perms.size()] );
        }
        catch ( NoSuchResourceStoreException e )
        {
            // what here?
            // it will be 404 later
            // TODO: this should be 404!
        }

        if ( result != null && result.length > 0 )
        {
            return super.isAccessAllowed( request, response, result );
        }
        else
        {
            if ( matched == null )
            {
                return false;
            }
            else
            {
                // we have a virtual path, allow it since if it here, it is probably anon or any other
                // already authenticated user
                if ( matched.getInvolvedRepositories() == 0 )
                {
                    return true;
                }
                else
                {
                    // no perms
                    // doing it with a fake perm, admin will match it anyway but nobody else :)
                    return super.isAccessAllowed( request, response, FAKE_PERMS );
                }
            }
        }

    }
}
