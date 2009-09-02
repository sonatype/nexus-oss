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
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

public abstract class AbstractNexusPlexusResource
    extends AbstractPlexusResource
    implements PlexusResource
{
    public static final String NEXUS_INSTANCE_LOCAL = "local";

    public static final String PASSWORD_PLACE_HOLDER = "|$|N|E|X|U|S|$|";
    
    @Requirement
    private Nexus nexus;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement( hint = "protected" )
    private RepositoryRegistry protectedRepositoryRegistry;

    @Requirement( hint = "default" )
    private RepositoryRegistry defaultRepositoryRegistry;
    
    @Requirement
    private ReferenceFactory referenceFactory;

    protected Nexus getNexus()
    {
        return nexus;
    }

    protected NexusConfiguration getNexusConfiguration()
    {
        return nexusConfiguration;
    }

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return protectedRepositoryRegistry;
    }
    
    protected RepositoryRegistry getUnprotectedRepositoryRegistry()
    {
        return defaultRepositoryRegistry;
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
        return this.referenceFactory.getContextRoot( request );
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
        return this.referenceFactory.createChildReference( request, childPath );
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

    protected Reference createRedirectReference( Request request )
    {
        String uriPart = request.getResourceRef().getTargetRef().toString().substring(
            request.getRootRef().getTargetRef().toString().length() );

        // trim leading slash
        if ( uriPart.startsWith( "/" ) )
        {
            uriPart = uriPart.substring( 1 );
        }

        Reference result = updateBaseRefPath( new Reference( getContextRoot( request ), uriPart ) ).getTargetRef();

        return result;
    }

    // ===

    protected PlexusContainer getPlexusContainer( Context context )
    {
        return (PlexusContainer) context.getAttributes().get( PlexusConstants.PLEXUS_KEY );
    }

    protected ErrorResponse getNexusErrorResponse( String id, String msg )
    {
        ErrorResponse ner = new ErrorResponse();
        ErrorMessage ne = new ErrorMessage();
        ne.setId( id );
        ne.setMsg( msg );
        ner.addError( ne );
        return ner;
    }

    protected void handleInvalidConfigurationException(
        InvalidConfigurationException e )
        throws PlexusResourceException
    {
        getLogger().warn( "Configuration error!", e );

        ErrorResponse nexusErrorResponse;

        ValidationResponse vr = e.getValidationResponse();

        if ( vr != null && vr.getValidationErrors().size() > 0 )
        {
            org.sonatype.configuration.validation.ValidationMessage vm = vr.getValidationErrors().get( 0 );
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

        ErrorResponse nexusErrorResponse;

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

        a.setGroupId( ai.groupId );

        a.setArtifactId( ai.artifactId );

        a.setVersion( ai.version );

        a.setClassifier( ai.classifier );

        a.setPackaging( ai.packaging );

        a.setRepoId( ai.repository );

        a.setContextId( ai.context );

        a.setPomLink( createPomLink( request, ai ) );

        a.setArtifactLink( createArtifactLink( request, ai ) );

        try
        {
            Repository repository = getUnprotectedRepositoryRegistry().getRepository( ai.repository );

            if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                MavenRepository mavenRepository = (MavenRepository) repository;

                Gav gav = new Gav(
                    ai.groupId,
                    ai.artifactId,
                    ai.version,
                    ai.classifier,
                    mavenRepository.getArtifactPackagingMapper().getExtensionForPackaging( ai.packaging ),
                    null,
                    null,
                    null,
                    VersionUtils.isSnapshot( ai.version ),
                    false,
                    null,
                    false,
                    null );

                ResourceStoreRequest req = new ResourceStoreRequest( mavenRepository.getGavCalculator().gavToPath( gav ) );

                a.setResourceURI( createRepositoryReference( request, ai.repository, req.getRequestPath() ).toString() );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "No such repository: '" + ai.repository + "'.", e );

            return null;
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            getLogger().warn( "Illegal artifact coordinate.", e );

            return null;
        }

        return a;
    }
    
    protected String createPomLink( Request request, ArtifactInfo ai )
    {
        if ( StringUtils.isNotEmpty( ai.classifier ) )
        {
            return "";
        }

        String suffix = "?r=" + ai.repository + "&g=" + ai.groupId + "&a=" + ai.artifactId + "&v=" + ai.version
            + "&p=pom";

        return createRedirectBaseRef( request ).toString() + suffix;
    }

    protected String createArtifactLink( Request request, ArtifactInfo ai )
    {
        if ( StringUtils.isEmpty( ai.packaging ) || "pom".equals( ai.packaging ) )
        {
            return "";
        }

        String suffix = "?r=" + ai.repository + "&g=" + ai.groupId + "&a=" + ai.artifactId + "&v=" + ai.version + "&p="
            + ai.packaging;

        if ( StringUtils.isNotBlank( ai.classifier ) )
        {
            suffix = suffix + "&c=" + ai.classifier;
        }

        return createRedirectBaseRef( request ).toString() + suffix;
    }
    
    protected Reference createRedirectBaseRef( Request request )
    {
        return createReference( getContextRoot( request ), "service/local/artifact/maven/redirect" ).getTargetRef();
    }

    protected String getValidRemoteIPAddress( Request request )
    {
        return RemoteIPFinder.findIP( request );
    }
    
    protected String getActualPassword( String newPassword, String oldPassword )
    {
        if( PASSWORD_PLACE_HOLDER.equals( newPassword ) )
        {
            return oldPassword;     
        }
        
        return newPassword;
    }
}
