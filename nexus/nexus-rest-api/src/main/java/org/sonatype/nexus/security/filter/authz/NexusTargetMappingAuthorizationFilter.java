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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.web.WebUtils;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;

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

    private NexusItemAuthorizer nexusItemAuthorizer;

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

    protected ResourceStoreRequest getResourceStoreRequest( ServletRequest request, boolean localOnly )
    {
        return new ResourceStoreRequest( getResourceStorePath( request ), localOnly );
    }

    @Override
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
            catch ( AccessDeniedException e )
            {
                // no access for read, so chances are post or put doesnt matter
                action = "post";
            }
            catch ( Exception e )
            {
                // huh?
                throw new IllegalStateException( "Got exception during target mapping!", e );
            }

            // the path exists, this is UPDATE
            return super.getActionFromHttpVerb( action );
        }
        else
        {
            return super.getActionFromHttpVerb( request );
        }
    }

    @Override
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

        if ( nexusItemAuthorizer == null )
        {
            PlexusContainer plexus = (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );

            try
            {
                nexusItemAuthorizer = plexus.lookup( NexusItemAuthorizer.class );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot lookup NexusArtifactAuthorizer!", e );
            }
        }
        
        return nexusItemAuthorizer.authorizePath(
            getResourceStoreRequest( request, false ),
            null,
            Action.valueOf( getActionFromHttpVerb( request ) ) );
    }
}
