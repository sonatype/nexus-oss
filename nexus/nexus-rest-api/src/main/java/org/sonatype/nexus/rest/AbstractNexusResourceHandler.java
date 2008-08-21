/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.fileupload.FileItemFactory;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.mgt.SecurityManager;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.NexusSecurityConfiguration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusError;
import org.sonatype.nexus.rest.model.NexusErrorResponse;
import org.sonatype.plexus.rest.AbstractPlexusAwareResource;
import org.sonatype.plexus.rest.PlexusRestletUtils;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

/**
 * Simple base class for Nexus specific restlets. We have some common code here, to access Nexus instance and to
 * instantaniate our custom Representation (serialization/deserialization).
 * 
 * @author cstamas
 */
public abstract class AbstractNexusResourceHandler
    extends AbstractPlexusAwareResource
{
    public AbstractNexusResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        getVariants().add( new Variant( MediaType.APPLICATION_XML ) );

        getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
    }

    /**
     * Gets the Nexus instance from request (which is placed in by NexusInstanceFilter.
     * 
     * @return
     */
    protected Nexus getNexus()
    {
        return (Nexus) getRequest().getAttributes().get( Nexus.ROLE );
    }

    protected NexusSecurityConfiguration getNexusSecurityConfiguration()
    {
        return (NexusSecurityConfiguration) getRequest().getAttributes().get( NexusSecurityConfiguration.ROLE );
    }

    protected SecurityManager getSecurityManager()
    {
        return (SecurityManager) getRequest().getAttributes().get( SecurityManager.class.getName() );
    }

    protected Object lookup( String role )
    {
        try
        {
            return PlexusRestletUtils.plexusLookup( getContext(), role );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot lookup role " + role, e );
        }
    }

    protected Object lookup( String role, String roleHint )
    {
        try
        {
            return PlexusRestletUtils.plexusLookup( getContext(), role, roleHint );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot lookup role " + role + " with roleHint " + roleHint, e );
        }
    }

    /**
     * Convert from ArtifactInfo to a NexusArtifact
     * 
     * @param ai
     * @return
     */
    protected NexusArtifact ai2Na( ArtifactInfo ai, boolean toParentCollection )
    {
        if ( ai == null )
        {
            return null;
        }

        NexusArtifact a = new NexusArtifact();

        try
        {
            Repository repository = getNexus().getRepository( ai.repository );

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

                // make path relative
                if ( path.startsWith( RepositoryItemUid.PATH_ROOT ) )
                {
                    path = path.substring( RepositoryItemUid.PATH_ROOT.length() );
                }

                path = "content/" + path;

                Reference repoRoot = calculateRepositoryReference( ai.repository );

                a.setResourceURI( calculateReference( repoRoot, path ).toString() );
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

    /**
     * Convert a collection of ArtifactInfo's to NexusArtifacts
     * 
     * @param aic
     * @return
     */
    protected Collection<NexusArtifact> ai2NaColl( Collection<ArtifactInfo> aic, boolean toParentCollection )
    {
        if ( aic == null )
        {
            return null;
        }
        List<NexusArtifact> result = new ArrayList<NexusArtifact>( aic.size() );
        for ( ArtifactInfo ai : aic )
        {
            result.add( ai2Na( ai, toParentCollection ) );
        }
        return result;
    }

    /**
     * For file uploads we are using commons-fileupload integration with restlet.org. We are storing one FileItemFactory
     * instance in context. This method simply encapsulates gettting it from Resource context.
     * 
     * @return
     */
    protected FileItemFactory getFileItemFactory()
    {
        return (FileItemFactory) getContext().getAttributes().get( ApplicationBridge.FILEITEM_FACTORY );
    }

    protected Date getModificationDate()
    {
        Date result = (Date) getContext().getAttributes().get( getRequest().getResourceRef().getPath() );

        if ( result == null )
        {
            ApplicationBridge application = (ApplicationBridge) getContext().getAttributes().get( Application.KEY );

            result = application.getCreatedOn();
        }

        return result;
    }

    protected void updateModificationDate( Reference reference )
    {
        getContext().getAttributes().put( reference.getPath(), new Date() );
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

    /**
     * Creates custom representation for supported Variants (XML and JSON).
     * 
     * @param variant
     * @return XStreamRepresentation instance for support variants, or null
     */
    protected XStreamRepresentation createRepresentation( Variant variant )
        throws IOException
    {
        XStreamRepresentation representation = null;

        String text = ( variant instanceof Representation ) ? ( (Representation) variant ).getText() : "";

        if ( MediaType.APPLICATION_JSON.equals( variant.getMediaType(), true ) )
        {
            representation = new XStreamRepresentation( (XStream) getContext().getAttributes().get(
                ApplicationBridge.JSON_XSTREAM ), text, variant.getMediaType() );
        }
        else if ( MediaType.APPLICATION_XML.equals( variant.getMediaType(), true ) )
        {
            representation = new XStreamRepresentation( (XStream) getContext().getAttributes().get(
                ApplicationBridge.XML_XSTREAM ), text, variant.getMediaType() );
        }

        representation.setModificationDate( getModificationDate() );

        return representation;
    }

    /**
     * Returns a representation that contains serialized payload.
     * 
     * @param variant
     * @param payload
     * @return
     */
    protected Representation serialize( Variant variant, Object payload )
    {
        if ( payload == null )
        {
            return null;
        }

        try
        {
            XStreamRepresentation result = createRepresentation( variant );

            if ( result == null )
            {
                return new StringRepresentation( payload.toString(), MediaType.TEXT_PLAIN, null, CharacterSet.UTF_8 );
            }
            else
            {
                result.setPayload( payload );

                return result;
            }
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Could not serialize entity!", e );

            return null;
        }
    }

    /**
     * Returns an Object deserialized from request Entity.
     * 
     * @param root
     * @return
     * @throws IOException
     */
    protected Object deserialize( Object root )
    {
        try
        {
            XStreamRepresentation result = createRepresentation( getRequest().getEntity() );

            if ( result == null )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_ACCEPTABLE );

                getLogger().log(
                    Level.SEVERE,
                    "Got an entity in unsupported variant:" + getRequest().getEntity().getMediaType().toString() );

                return null;
            }
            else
            {
                return result.getPayload( root );
            }
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Could not get the representation entity!", e );

            return null;
        }
    }

    public void handleGet()
    {
        // safe method
        super.handleGet();

        if ( getResponse().getEntity() == null )
        {
            getResponse().setEntity( new StringRepresentation( "", MediaType.TEXT_PLAIN ) );
        }
    }

    public void handleHead()
    {
        // safe method
        super.handleHead();

        if ( getResponse().getEntity() == null )
        {
            getResponse().setEntity( new StringRepresentation( "", MediaType.TEXT_PLAIN ) );
        }
    }

    public void handlePost()
    {
        // changes TS
        super.handlePost();

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( getRequest().getResourceRef() );
        }

        if ( getResponse().getEntity() == null )
        {
            getResponse().setEntity( new StringRepresentation( "", MediaType.TEXT_PLAIN ) );
        }
    }

    public void handlePut()
    {
        // changes TS and parents TS
        super.handlePut();

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( getRequest().getResourceRef() );

            updateModificationDate( getRequest().getResourceRef().getParentRef() );
        }

        if ( getResponse().getEntity() == null )
        {
            getResponse().setEntity( new StringRepresentation( "", MediaType.TEXT_PLAIN ) );
        }
    }

    public void handleDelete()
    {
        // changes TS and parents TS
        super.handleDelete();

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( getRequest().getResourceRef() );

            updateModificationDate( getRequest().getResourceRef().getParentRef() );
        }

        if ( getResponse().getEntity() == null )
        {
            getResponse().setEntity( new StringRepresentation( "", MediaType.TEXT_PLAIN ) );
        }
    }

    /**
     * Returns the representation of Resource. This is actually only a wrapper to handle some errors.
     * 
     * @param variant
     */
    public final Representation getRepresentation( Variant variant )
    {
        if ( getRequest().getConditions().hasSome() )
        {
            if ( getRequest().getConditions().getModifiedSince() != null )
            {
            }
            else if ( getRequest().getConditions().getUnmodifiedSince() != null )
            {

            }
        }

        try
        {
            return getRepresentationHandler( variant );
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );

            return null;
        }
        catch ( Exception e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got Exception!", e );

            return null;
        }
    }

    /**
     * Get the Representation for this particular Variant
     * 
     * @param variant
     * @return
     * @throws Exception
     */
    protected Representation getRepresentationHandler( Variant variant )
        throws Exception
    {
        return super.getRepresentation( variant );
    }

    /**
     * Calculates a subReference.
     * 
     * @param relPart
     * @return
     */
    protected Reference calculateSubReference( String relPart )
    {
        return calculateReference( getRequest().getResourceRef(), relPart ).getTargetRef();
    }

    /**
     * Calculates a reference to Repository based on Repository ID.
     * 
     * @param base
     * @param repoId
     * @return
     */
    protected Reference calculateRepositoryReference( String repoId )
    {
        return calculateReference( getRequest().getRootRef().getParentRef(), "service/local/repositories/" + repoId )
            .getTargetRef();
    }

    /**
     * Calculates Reference.
     * 
     * @param base
     * @param relPart
     * @return
     */
    protected Reference calculateReference( Reference base, String relPart )
    {
        Reference ref = new Reference( base, relPart );

        if ( !ref.getBaseRef().getPath().endsWith( "/" ) )
        {
            ref.getBaseRef().setPath( ref.getBaseRef().getPath() + "/" );
        }

        return ref.getTargetRef();
    }

    protected void handleConfigurationException( ConfigurationException e, Representation representation )
    {
        getLogger().log( Level.WARNING, "Configuration error!", e );

        getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );

        if ( InvalidConfigurationException.class.isAssignableFrom( e.getClass() ) )
        {
            ValidationResponse vr = ( (InvalidConfigurationException) e ).getValidationResponse();

            if ( vr != null && vr.getValidationErrors().size() > 0 )
            {
                ValidationMessage vm = vr.getValidationErrors().get( 0 );
                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse( vm.getKey(), vm.getShortMessage() ) ) );
            }
            else
            {
                getResponse().setEntity( serialize( representation, getNexusErrorResponse( "*", e.getMessage() ) ) );
            }
        }
        else
        {
            getResponse().setEntity( serialize( representation, getNexusErrorResponse( "*", e.getMessage() ) ) );
        }
    }

}
