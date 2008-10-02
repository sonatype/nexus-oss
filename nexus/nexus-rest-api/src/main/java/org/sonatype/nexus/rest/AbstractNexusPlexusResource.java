package org.sonatype.nexus.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
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
    public static final String NEXUS_INSTANCE_KEY = "instanceName";

    public static final String NEXUS_INSTANCE_LOCAL = "local";

    /**
     * @plexus.requirement
     */
    private Nexus nexus;

    /**
     * @plexus.requirement
     */
    private NexusItemAuthorizer nexusItemAuthorizer;

    
    public String getPermissionPrefix()
    {
        return "";
    }

    protected Nexus getNexusInstance( Request request )
        throws ResourceException
    {
        if ( NEXUS_INSTANCE_LOCAL.equals( request.getAttributes().get( NEXUS_INSTANCE_KEY ) ) )
        {
            return nexus;
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Nexus instance named '"
                + request.getAttributes().get( NEXUS_INSTANCE_KEY ) + "' not found!" );
        }
    }

    protected Reference createChildReference( Request request, String childPath )
    {
        Reference result = new Reference( request.getResourceRef() ).addSegment( childPath ).getTargetRef();

        if ( result.hasQuery() )
        {
            result.setQuery( null );
        }

        return result;
    }

    protected Reference createRootReference( Request request, String relPart )
    {
        Reference ref = new Reference( request.getRootRef().getParentRef(), relPart );

        if ( !ref.getBaseRef().getPath().endsWith( "/" ) )
        {
            ref.getBaseRef().setPath( ref.getBaseRef().getPath() + "/" );
        }

        return ref.getTargetRef();
    }

    /**
     * Calculates a reference to Repository based on Repository ID.
     * 
     * @param base
     * @param repoId
     * @return
     */
    protected Reference createRepositoryReference( Request request, String repoId )
    {
        return createReference( request.getRootRef().getParentRef(), "service/local/repositories/" + repoId )
            .getTargetRef();
    }

    /**
     * Calculates Reference.
     * 
     * @param base
     * @param relPart
     * @return
     */
    protected Reference createReference( Reference base, String relPart )
    {
        Reference ref = new Reference( base, relPart );

        if ( !ref.getBaseRef().getPath().endsWith( "/" ) )
        {
            ref.getBaseRef().setPath( ref.getBaseRef().getPath() + "/" );
        }

        return ref.getTargetRef();
    }

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
        org.sonatype.jsecurity.realms.tools.InvalidConfigurationException e ) throws PlexusResourceException
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
        
        throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", nexusErrorResponse );
    }

    protected void handleConfigurationException( ConfigurationException e ) throws PlexusResourceException
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
        
        throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", nexusErrorResponse );
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

                if ( !nexusItemAuthorizer.authorizePath( mr.createUid( path ), null, Action.read ) )
                {
                    return null;
                }

                // make path relative
                if ( path.startsWith( RepositoryItemUid.PATH_ROOT ) )
                {
                    path = path.substring( RepositoryItemUid.PATH_ROOT.length() );
                }

                path = "content/" + path;

                Reference repoRoot = createRepositoryReference( request, ai.repository );

                a.setResourceURI( createReference( repoRoot, path ).toString() );
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
}
