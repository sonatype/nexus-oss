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
package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.StorageFileItemRepresentation;

public abstract class AbstractArtifactPlexusResource
    extends AbstractNexusPlexusResource
{
    /**
     * Centralized way to create ResourceStoreRequests, since we have to fill in various things in Request context, like
     * authenticated username, etc.
     * 
     * @param isLocal
     * @return
     */
    protected ArtifactStoreRequest getResourceStoreRequest( Request request, boolean localOnly, String repositoryId,
        String g, String a, String v, String p, String c, String e )
        throws ResourceException
    {
        if ( StringUtils.isBlank( p ) && StringUtils.isBlank( e ) )
        {
            // if packaging and extension is both blank, it is a bad request
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Deployment tried with both 'packaging' and/or 'extension' being empty! One of these values is mandatory!" );
        }

        MavenRepository mavenRepository = getMavenRepository( repositoryId );

        // if extension is not given, fall-back to packaging and apply mapper
        if ( StringUtils.isBlank( e ) )
        {
            e = mavenRepository.getArtifactPackagingMapper().getExtensionForPackaging( p );
        }

        // clean up the classifier
        if ( StringUtils.isBlank( c ) )
        {
            c = null;
        }

        Gav gav = null; 
            
        try
        {
            gav = new Gav( g, a, v, c, e, null, null, null, VersionUtils.isSnapshot( v ), false, null, false, null );
        }
        catch ( IllegalArtifactCoordinateException ex )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Illegal artifact coordinate.", ex );
        }
            
        ArtifactStoreRequest result = new ArtifactStoreRequest( mavenRepository, gav, localOnly );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Created ArtifactStoreRequest request for " + result.getRequestPath() );
        }

        // stuff in the originating remote address
        result.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, getValidRemoteIPAddress( request ) );

        // stuff in the user id if we have it in request
        if ( request.getChallengeResponse() != null && request.getChallengeResponse().getIdentifier() != null )
        {
            result.getRequestContext().put( AccessManager.REQUEST_USER, request.getChallengeResponse().getIdentifier() );
        }

        // this is HTTPS, get the cert and stuff it too for later
        if ( request.isConfidential() )
        {
            result.getRequestContext().put( AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE );

            List<?> certs = (List<?>) request.getAttributes().get( "org.restlet.https.clientCertificates" );

            if ( certs != null )
            {
                result.getRequestContext().put( AccessManager.REQUEST_CERTIFICATES, certs );
            }
        }

        // put the incoming URLs
        result.setRequestAppRootUrl( getContextRoot( request ).toString() );
        result.setRequestUrl( request.getOriginalRef().toString() );

        return result;
    }

    protected Model getPom( Variant variant, Request request, Response response )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        // TODO: enable only one section retrieval of POM, ie. only mailing lists, or team members

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String repositoryId = form.getFirstValue( "r" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        ArtifactStoreRequest gavRequest = getResourceStoreRequest(
            request,
            false,
            repositoryId,
            groupId,
            artifactId,
            version,
            null,
            null,
            "pom" );

        try
        {
            MavenRepository mavenRepository = getMavenRepository( repositoryId );

            ArtifactStoreHelper helper = mavenRepository.getArtifactStoreHelper();

            InputStream pomContent = null;

            InputStreamReader ir = null;

            Model pom = null;

            try
            {
                StorageFileItem file = helper.retrieveArtifactPom( gavRequest );

                pomContent = file.getInputStream();

                MavenXpp3Reader reader = new MavenXpp3Reader();

                ir = new InputStreamReader( pomContent );

                pom = reader.read( ir );
            }
            finally
            {
                if ( ir != null )
                {
                    ir.close();
                }
                if ( pomContent != null )
                {
                    pomContent.close();
                }
            }

            return pom;

        }
        catch ( Exception e )
        {
            handleException( request, response, e );
        }

        return null;
    }

    protected Object getContent( Variant variant, boolean redirectTo, Request request, Response response )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String packaging = form.getFirstValue( "p" );

        String classifier = form.getFirstValue( "c" );

        String repositoryId = form.getFirstValue( "r" );

        String extension = form.getFirstValue( "e" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        // default the packaging
        if ( StringUtils.isBlank( packaging ) )
        {
            packaging = "jar";
        }

        ArtifactStoreRequest gavRequest = getResourceStoreRequest(
            request,
            false,
            repositoryId,
            groupId,
            artifactId,
            version,
            packaging,
            classifier,
            extension );

        try
        {
            MavenRepository mavenRepository = getMavenRepository( repositoryId );

            ArtifactStoreHelper helper = mavenRepository.getArtifactStoreHelper();

            StorageFileItem file = helper.retrieveArtifact( gavRequest );

            if ( redirectTo )
            {
                Reference fileReference = createRepositoryReference( request, file
                    .getRepositoryItemUid().getRepository().getId(), file.getRepositoryItemUid().getPath() );

                response.setLocationRef( fileReference );

                response.setStatus( Status.REDIRECTION_PERMANENT );

                String redirectMessage = "If you are not automatically redirected use this url: "
                    + fileReference.toString();
                return redirectMessage;
            }
            else
            {
                Representation result = new StorageFileItemRepresentation( file );

                result.setDownloadable( true );

                result.setDownloadName( file.getName() );

                return result;
            }

        }
        catch ( Exception e )
        {
            handleException( request, response, e );
        }

        return null;
    }

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        // we have "nibbles": (params,fileA,[fileB])+
        // the second file is optional
        // if two files are present, one of them should be POM
        String repositoryId = null;

        boolean hasPom = false;

        boolean isPom = false;

        InputStream is = null;

        String groupId = null;

        String artifactId = null;

        String version = null;

        String classifier = null;

        String packaging = null;

        String extension = null;

        PomArtifactManager pomManager = new PomArtifactManager( getNexus()
            .getNexusConfiguration().getTemporaryDirectory() );

        try
        {
            for ( FileItem fi : files )
            {
                if ( fi.isFormField() )
                {
                    // a parameter
                    if ( "r".equals( fi.getFieldName() ) )
                    {
                        repositoryId = fi.getString();
                    }
                    else if ( "g".equals( fi.getFieldName() ) )
                    {
                        groupId = fi.getString();
                    }
                    else if ( "a".equals( fi.getFieldName() ) )
                    {
                        artifactId = fi.getString();
                    }
                    else if ( "v".equals( fi.getFieldName() ) )
                    {
                        version = fi.getString();
                    }
                    else if ( "p".equals( fi.getFieldName() ) )
                    {
                        packaging = fi.getString();
                    }
                    else if ( "c".equals( fi.getFieldName() ) )
                    {
                        classifier = fi.getString();
                    }
                    else if ( "e".equals( fi.getFieldName() ) )
                    {
                        extension = fi.getString();
                    }
                    else if ( "hasPom".equals( fi.getFieldName() ) )
                    {
                        hasPom = Boolean.parseBoolean( fi.getString() );
                    }
                }
                else
                {
                    // a file
                    isPom = fi.getName().endsWith( ".pom" ) || fi.getName().endsWith( "pom.xml" );

                    PomArtifactManager.ArtifactCoordinate coords = null;

                    ArtifactStoreRequest gavRequest = null;

                    if ( hasPom )
                    {
                        if ( isPom )
                        {
                            // let it "thru" the pomManager to be able to get GAV from it on later pass
                            pomManager.storeTempPomFile( fi.getInputStream() );

                            is = pomManager.getTempPomFileInputStream();
                        }
                        else
                        {
                            is = fi.getInputStream();
                        }

                        try
                        {
                            coords = pomManager.getArtifactCoordinateFromTempPomFile();
                        }
                        catch ( IOException e )
                        {
                            getLogger().info( e.getMessage() );

                            throw new ResourceException(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Error occurred while reading the POM file. Malformed POM?" );
                        }

                        if ( isPom )
                        {
                            gavRequest = getResourceStoreRequest(
                                request,
                                true,
                                repositoryId,
                                coords.getGroupId(),
                                coords.getArtifactId(),
                                coords.getVersion(),
                                coords.getPackaging(),
                                null,
                                null );
                        }
                        else
                        {
                            gavRequest = getResourceStoreRequest(
                                request,
                                true,
                                repositoryId,
                                coords.getGroupId(),
                                coords.getArtifactId(),
                                coords.getVersion(),
                                coords.getPackaging(),
                                classifier,
                                extension );
                        }
                    }
                    else
                    {
                        is = fi.getInputStream();

                        gavRequest = getResourceStoreRequest(
                            request,
                            true,
                            repositoryId,
                            groupId,
                            artifactId,
                            version,
                            packaging,
                            classifier,
                            extension );
                    }

                    MavenRepository mr = gavRequest.getMavenRepository();

                    ArtifactStoreHelper helper = mr.getArtifactStoreHelper();

                    // temporarily we disable SNAPSHOT upload
                    // check is it a Snapshot repo
                    if ( RepositoryPolicy.SNAPSHOT.equals( mr.getRepositoryPolicy() ) )
                    {
                        getLogger().info(
                            "Upload to SNAPSHOT maven repository attempted, returning Bad Request." );

                        throw new ResourceException(
                            Status.CLIENT_ERROR_BAD_REQUEST,
                            "This is a Maven SNAPSHOT repository, and manual upload against it is forbidden!" );
                    }

                    if ( !versionMatchesPolicy( gavRequest.getVersion(), mr.getRepositoryPolicy() ) )
                    {
                        getLogger().warn(
                            "Version (" + gavRequest.getVersion() + ") and Repository Policy mismatch" );
                        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "The version "
                            + gavRequest.getVersion() + " does not match the repository policy!" );
                    }

                    if ( isPom )
                    {
                        helper.storeArtifactPom( gavRequest, is, null );

                        isPom = false;
                    }
                    else
                    {
                        if ( hasPom )
                        {
                            helper.storeArtifact( gavRequest, is, null );
                        }
                        else
                        {
                            helper.storeArtifactWithGeneratedPom( gavRequest, packaging, is, null );
                        }
                    }
                }
            }
        }
        catch ( Throwable t )
        {            
            return buildUploadFailedHtmlResponse( t, request, response );
        }
        finally
        {
            if ( hasPom )
            {
                pomManager.removeTempPomFile();
            }
        }        

        return null;
    }
    
    protected String buildUploadFailedHtmlResponse( Throwable t, Request request, Response response )
    {
        getLogger().debug( "Got error while uploading artifact", t );
        
        StringBuffer resp = new StringBuffer();
        resp.append( "<html>" );
        resp.append( "<body>" );
        resp.append( "<error>" + t.getMessage() + "</error>" );
        resp.append( "</body>" );
        resp.append( "</html>" );

        String forceSuccess = request.getResourceRef().getQueryAsForm().getFirstValue( "forceSuccess" );
        
        if ( !"true".equals( forceSuccess ))
        {
            if ( t instanceof ResourceException )
            {
                response.setStatus( ( ( ResourceException ) t ).getStatus() );
            }
            else
            {
                response.setStatus( Status.SERVER_ERROR_INTERNAL );
            }
        }
        
        return resp.toString();
    }

    protected void handleException( Request request, Response res, Exception t )
        throws ResourceException
    {
        if ( t instanceof ResourceException )
        {
            throw (ResourceException) t;
        }
        else if ( t instanceof IllegalArgumentException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal argument:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotAvailableException )
        {
            throw new ResourceException( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, t.getMessage() );
        }
        else if ( t instanceof IllegalRequestException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal request:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof IllegalOperationException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal operation:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof StorageException )
        {
            getLogger().warn( "IO problem!", t );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
        else if ( t instanceof UnsupportedStorageOperationException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, t.getMessage() );
        }
        else if ( t instanceof NoSuchResourceStoreException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof ItemNotFoundException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof AccessDeniedException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED, t.getMessage() );
        }
        else if ( t instanceof XmlPullParserException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof IOException )
        {
            getLogger().warn( "IO error!", t );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
        else
        {
            getLogger().warn( t.getMessage(), t );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
    }

    protected MavenRepository getMavenRepository( String id )
        throws ResourceException
    {
        try
        {
            Repository repository = getUnprotectedRepositoryRegistry().getRepository( id );

            if ( !repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );
            }

            return repository.adaptToFacet( MavenRepository.class );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

    protected boolean versionMatchesPolicy( String version, RepositoryPolicy policy )
    {
        boolean result = false;

        if ( ( RepositoryPolicy.SNAPSHOT.equals( policy ) && VersionUtils.isSnapshot( version ) )
            || ( RepositoryPolicy.RELEASE.equals( policy ) && !VersionUtils.isSnapshot( version ) ) )
        {
            result = true;
        }

        return result;
    }
    
}
