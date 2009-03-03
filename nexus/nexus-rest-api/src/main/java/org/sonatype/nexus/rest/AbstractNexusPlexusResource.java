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
package org.sonatype.nexus.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItemFactory;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusError;
import org.sonatype.nexus.rest.model.NexusErrorResponse;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

public abstract class AbstractNexusPlexusResource
    extends AbstractPlexusResource
    implements PlexusResource
{
    public static final String NEXUS_INSTANCE_LOCAL = "local";

    @Requirement
    private Nexus nexus;

    @Requirement
    private NexusItemAuthorizer nexusItemAuthorizer;

    protected Nexus getNexus()
    {
        return nexus;
    }

    /**
     * For file uploads we are using commons-fileupload integration with restlet.org. We are storing one FileItemFactory
     * instance in context. This method simply encapsulates gettting it from Resource context.
     * 
     * @return
     */
    protected FileItemFactory getFileItemFactory( Context context )
    {
        return (FileItemFactory) context.getAttributes().get( NexusApplication.FILEITEM_FACTORY );
    }

    /**
     * Centralized, since this is the only "dependent" stuff that relies on knowledge where restlet.Application is
     * mounted (we had a /service => / move).
     * 
     * @param request
     * @return
     */
    protected Reference getContextRoot( Request request )
    {
        Reference result = null;

        if ( getNexus().isForceBaseUrl() && getNexus().getBaseUrl() != null )
        {
            result = new Reference( getNexus().getBaseUrl() );
        }
        else
        {
            result = request.getRootRef();
        }

        // fix for when restlet is at webapp root
        if ( StringUtils.isEmpty( result.getPath() ) )
        {
            result.setPath( "/" );
        }

        return result;
    }
    
    private Reference updateBaseRefPath( Reference reference )
    {
        if ( reference.getBaseRef().getPath() == null )
        {
            reference.getBaseRef().setPath( "/" );
        }
        else if ( !reference.getBaseRef().getPath().endsWith( "/" ) )
        {
            reference.getBaseRef().setPath( reference.getBaseRef().getPath() + "/" );
        }
        
        return reference;
    }

    protected Reference createReference( Reference base, String relPart )
    {
        Reference ref = new Reference( base, relPart );

        return updateBaseRefPath( ref ).getTargetRef();
    }

    protected Reference createChildReference( Request request, PlexusResource resource, String childPath )
    {
        String uriPart = request.getResourceRef().getTargetRef().toString().substring(
            request.getRootRef().getTargetRef().toString().length() );
        
        // trim leading slash
        if ( uriPart.startsWith( "/" ) )
        {
            uriPart = uriPart.substring( 1 );
        }
        
        Reference result = updateBaseRefPath( new Reference( getContextRoot( request ),  uriPart ) ).addSegment( childPath );

        if ( result.hasQuery() )
        {
            result.setQuery( null );
        }

        return result.getTargetRef();
    }

    protected Reference createRootReference( Request request, String relPart )
    {
        Reference ref = new Reference( getContextRoot( request ), relPart );

        if ( !ref.getBaseRef().getPath().endsWith( "/" ) )
        {
            ref.getBaseRef().setPath( ref.getBaseRef().getPath() + "/" );
        }

        return ref.getTargetRef();
    }
    
    protected Reference createRepositoryContentReference( Request request, String repoId )
    {
        return createReference( getContextRoot( request ), "content/repositories/" + repoId ).getTargetRef();
    }

    protected Reference createRepositoryReference( Request request, String repoId )
    {
        return createReference( getContextRoot( request ), "service/local/repositories/" + repoId ).getTargetRef();
    }

    protected Reference createRepositoryReference( Request request, String repoId, String repoPath )
    {
        Reference repoRootRef = createRepositoryReference( request, repoId );

        if ( repoPath.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            repoPath = repoPath.substring( 1 );
        }

        repoPath = "content/" + repoPath;

        return createReference( repoRootRef, repoPath );
    }

    protected Reference createRepositoryGroupReference( Request request, String groupId )
    {
        return createReference( getContextRoot( request ), "service/local/repo_groups/" + groupId ).getTargetRef();
    }

    protected Reference createRepositoryGroupReference( Request request, String groupId, String repoPath )
    {
        Reference groupRootRef = createRepositoryGroupReference( request, groupId );

        if ( repoPath.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            repoPath = repoPath.substring( 1 );
        }

        repoPath = "content/" + repoPath;

        return createReference( groupRootRef, repoPath );
    }

    // ===

    protected PlexusContainer getPlexusContainer( Context context )
    {
        return (PlexusContainer) context.getAttributes().get( PlexusConstants.PLEXUS_KEY );
    }

    protected NexusErrorResponse getNexusErrorResponse( String id, String msg )
    {
        NexusErrorResponse ner = new NexusErrorResponse();
        NexusError ne = new NexusError();
        ne.setId( id );
        ne.setMsg( msg );
        ner.addError( ne );
        return ner;
    }

    protected void handleInvalidConfigurationException(
        org.sonatype.jsecurity.realms.tools.InvalidConfigurationException e )
        throws PlexusResourceException
    {
        getLogger().warn( "Configuration error!", e );

        NexusErrorResponse nexusErrorResponse;

        org.sonatype.jsecurity.realms.validator.ValidationResponse vr = e.getValidationResponse();

        if ( vr != null && vr.getValidationErrors().size() > 0 )
        {
            org.sonatype.jsecurity.realms.validator.ValidationMessage vm = vr.getValidationErrors().get( 0 );
            nexusErrorResponse = getNexusErrorResponse( vm.getKey(), vm.getShortMessage() );
        }
        else
        {
            nexusErrorResponse = getNexusErrorResponse( "*", e.getMessage() );
        }

        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", nexusErrorResponse );
    }

    protected void handleConfigurationException( ConfigurationException e )
        throws PlexusResourceException
    {
        getLogger().warn( "Configuration error!", e );

        NexusErrorResponse nexusErrorResponse;

        if ( InvalidConfigurationException.class.isAssignableFrom( e.getClass() ) )
        {
            ValidationResponse vr = ( (InvalidConfigurationException) e ).getValidationResponse();

            if ( vr != null && vr.getValidationErrors().size() > 0 )
            {
                ValidationMessage vm = vr.getValidationErrors().get( 0 );
                nexusErrorResponse = getNexusErrorResponse( vm.getKey(), vm.getShortMessage() );
            }
            else
            {
                nexusErrorResponse = getNexusErrorResponse( "*", e.getMessage() );
            }
        }
        else
        {
            nexusErrorResponse = getNexusErrorResponse( "*", e.getMessage() );
        }

        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", nexusErrorResponse );
    }

    /**
     * Convert a collection of ArtifactInfo's to NexusArtifacts
     * 
     * @param aic
     * @return
     */
    protected Collection<NexusArtifact> ai2NaColl( Request request, Collection<ArtifactInfo> aic )
    {
        if ( aic == null )
        {
            return null;
        }
        List<NexusArtifact> result = new ArrayList<NexusArtifact>( aic.size() );
        for ( ArtifactInfo ai : aic )
        {
            NexusArtifact na = ai2Na( request, ai );

            if ( na != null )
            {
                result.add( na );
            }
        }
        return result;
    }

    /**
     * Convert from ArtifactInfo to a NexusArtifact
     * 
     * @param ai
     * @return
     */
    protected NexusArtifact ai2Na( Request request, ArtifactInfo ai )
    {
        if ( ai == null )
        {
            return null;
        }

        NexusArtifact a = new NexusArtifact();

        try
        {
            Repository repository = nexus.getRepository( ai.repository );

            if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                MavenRepository mr = (MavenRepository) repository;

                Gav gav = new Gav(
                    ai.groupId,
                    ai.artifactId,
                    ai.version,
                    ai.classifier,
                    mr.getArtifactPackagingMapper().getExtensionForPackaging( ai.packaging ),
                    null,
                    null,
                    null,
                    VersionUtils.isSnapshot( ai.version ),
                    false,
                    null,
                    false,
                    null );

                String path = mr.getGavCalculator().gavToPath( gav );

                if ( !nexusItemAuthorizer.authorizePath( mr, path, null, Action.read ) )
                {
                    return null;
                }

                a.setResourceURI( createRepositoryReference( request, ai.repository, path ).toString() );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            return null;
        }

        a.setGroupId( ai.groupId );

        a.setArtifactId( ai.artifactId );

        a.setVersion( ai.version );

        a.setClassifier( ai.classifier );

        a.setPackaging( ai.packaging );

        a.setRepoId( ai.repository );

        a.setContextId( ai.context );

        return a;
    }
    
    protected String getValidRemoteIPAddress( Request request )
    {
        Form form = (Form) request.getAttributes().get("org.restlet.http.headers");
        String forwardedIP = getFirstForwardedIp( form.getFirstValue( "X-Forwarded-For" ) );
        
        if ( forwardedIP != null )
        {
            return forwardedIP;
        }
        
        List<String> ipAddresses = request.getClientInfo().getAddresses();
        
        if ( ipAddresses.size() > 0 )
        {
            return ipAddresses.get( 0 );
        }
        
        return null;
    }
    
    protected String getFirstForwardedIp( String forwardedFor )
    {
        if ( !StringUtils.isEmpty( forwardedFor ) )
        {
            String [] forwardedIps = forwardedFor.split( "," );
            
            return forwardedIps[0].trim();
        }
        
        return null;
    }
}
