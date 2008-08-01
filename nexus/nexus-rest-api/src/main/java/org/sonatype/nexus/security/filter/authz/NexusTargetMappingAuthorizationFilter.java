package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
    private String pathPrefix;

    public String getPathPrefix()
    {
        return pathPrefix;
    }

    public void setPathPrefix( String pathPrefix )
    {
        this.pathPrefix = pathPrefix;
    }

    public String getResourceStorePath( ServletRequest request )
    {
        if ( getPathPrefix() != null )
        {
            String path = WebUtils.getPathWithinApplication( (HttpServletRequest) request );

            path = path.replaceFirst( getPathPrefix(), "" );

            return path;
        }
        else
        {
            return null;
        }
    }

    public String[] getTargetPerms( TargetSet matched )
    {
        String[] result = null;

        List<String> perms = new ArrayList<String>( matched.getMatches().size() );

        // nexus : 'target' + targetId : repoId
        for ( TargetMatch match : matched.getMatches() )
        {
            perms.add( "nexus:target" + match.getTarget().getId() + ":" + match.getRepository().getId() );
        }

        result = perms.toArray( new String[perms.size()] );

        return result;
    }

    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        // let check the mappedValues 1st

        boolean result = super.isAccessAllowed( request, response, mappedValue );

        // if we are not allowed at start, forbid it
        if ( !result )
        {
            return false;
        }

        // if we are allowed to go forward, let's check the target perms
        ResourceStoreRequest rsr = new ResourceStoreRequest( getResourceStorePath( request ), true );

        TargetSet matched = getNexus( request ).getRootRouter().getTargetsForRequest( rsr );

        if ( matched.getMatchedRepositoryIds().size() > 0 )
        {
            String[] targetPerms = getTargetPerms( matched );

            String action = getActionFromHttpVerb( request );

            String[] mappedPerms = mapPerms( targetPerms, action );

            return super.isAccessAllowed( request, response, mappedPerms );
        }
        else
        {
            // this will hit no repos, it is a virtual path, allow access
            return true;
        }
    }
}
