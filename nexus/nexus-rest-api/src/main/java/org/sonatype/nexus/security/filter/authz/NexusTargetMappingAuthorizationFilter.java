package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jsecurity.subject.Subject;
import org.jsecurity.web.WebUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
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
    private Pattern pathPrefixPattern;

    private String pathPrefix;

    private String pathReplacement;

    public String getPathPrefix()
    {
        return pathPrefix;
    }

    public void setPathPrefix( String pathPrefix )
    {
        this.pathPrefix = pathPrefix;

        if ( pathPrefix != null )
        {
            pathPrefixPattern = Pattern.compile( pathPrefix );
        }
    }

    public String getPathReplacement()
    {
        if ( pathReplacement == null )
        {
            pathReplacement = "";
        }

        return pathReplacement;
    }

    public void setPathReplacement( String pathReplacement )
    {
        this.pathReplacement = pathReplacement;
    }

    public String getResourceStorePath( ServletRequest request )
    {
        String path = WebUtils.getPathWithinApplication( (HttpServletRequest) request );

        if ( getPathPrefix() != null )
        {
            Matcher m = pathPrefixPattern.matcher( path );

            if ( m.matches() )
            {
                path = getPathReplacement();

                // TODO: hardcoded currently
                if ( path.contains( "@1" ) )
                {
                    path = path.replaceAll( "@1", m.group( 1 ) );
                }

                if ( path.contains( "@2" ) )
                {
                    path = path.replaceAll( "@2", m.group( 2 ) );
                }
                // and so on... this will be reworked to be dynamic
            }
            else
            {
                throw new IllegalArgumentException(
                    "The request path does not matches the incoming request? This is misconfiguration in web.xml!" );
            }
        }

        return path;
    }

    public String[] getTargetPerms( TargetSet matched )
    {
        String[] result = null;

        List<String> perms = new ArrayList<String>( matched.getMatches().size() );

        // nexus : 'target' + targetId : repoId
        for ( TargetMatch match : matched.getMatches() )
        {
            perms.add( "nexus:target:" + match.getTarget().getId() + ":" + match.getRepository().getId() );
        }

        result = perms.toArray( new String[perms.size()] );

        return result;
    }

    protected String getActionFromHttpVerb( ServletRequest request )
    {
        String action = ( (HttpServletRequest) request ).getMethod().toLowerCase();

        if ( "put".equals( action ) )
        {
            // heavy handed thing
            // doing a LOCAL ONLY request to check is this exists?
            try
            {
                getNexus( request ).getRootRouter().retrieveItem( getResourceStoreRequest( request, true ) );
            }
            catch ( ItemNotFoundException e )
            {
                // the path does not exists, it is a CREATE
                action = "post";
            }
            catch ( Exception e )
            {
                // huh?
            }

            // the path exists, this is UPDATE
            return super.getActionFromHttpVerb( action );
        }
        else
        {
            return super.getActionFromHttpVerb( request );
        }
    }

    protected ResourceStoreRequest getResourceStoreRequest( ServletRequest request, boolean localOnly )
    {
        return new ResourceStoreRequest( getResourceStorePath( request ), localOnly );
    }

    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        // let check the mappedValues 1st
        boolean result = false;

        if ( mappedValue != null )
        {
            result = super.isAccessAllowed( request, response, mappedValue );

            // if we are not allowed at start, forbid it
            if ( !result )
            {
                return false;
            }
        }

        // collect the targetSet/matches for the request
        TargetSet matched = getNexus( request ).getRootRouter().getTargetsForRequest(
            getResourceStoreRequest( request, true ) );

        // did we hit repositories at all?
        if ( matched.getMatchedRepositoryIds().size() > 0 )
        {
            // we had reposes affected, check the targets
            // make perms from TargetSet
            String[] targetPerms = getTargetPerms( matched );

            // get the action from HTTP verb
            String action = getActionFromHttpVerb( request );

            // append the action to the end of targetPerms
            String[] mappedPerms = mapPerms( targetPerms, action );

            // get the subject for testing perms
            Subject subject = getSubject( request, response );

            // iterator over perms, and if any of them is permitted for subject
            // allow access
            for ( String perm : mappedPerms )
            {
                if ( subject.isPermitted( perm ) )
                {
                    return true;
                }
            }

            // did not hit any of perms, so the subject is not allowed to do this
            return false;
        }
        else
        {
            // we hit no repos, it is a virtual path, allow access
            return true;
        }
    }
}
