/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.StorageFileItemRepresentation;
import org.sonatype.nexus.security.filter.authc.NexusHttpAuthenticationFilter;

import com.noelios.restlet.ext.servlet.ServletCall;
import com.noelios.restlet.http.HttpRequest;

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
        String repositoryGroupId, String g, String a, String v, String p, String c, String e )
    {
        ArtifactStoreRequest result = new ArtifactStoreRequest(
            localOnly,
            repositoryId,
            repositoryGroupId,
            g,
            a,
            v,
            p,
            c,
            e );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Created ResourceStore request for " + result.getRequestPath() );
        }

        result.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, request.getClientInfo().getAddress() );

        if ( request.getChallengeResponse() != null && request.getChallengeResponse().getIdentifier() != null )
        {
            result.getRequestContext().put( AccessManager.REQUEST_USER, request.getChallengeResponse().getIdentifier() );
        }

        if ( request.isConfidential() )
        {
            result.getRequestContext().put( AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE );

            // X509Certificate[] certs = (X509Certificate[]) context.getHttpServletRequest().getAttribute(
            // "javax.servlet.request.X509Certificate" );
            // if ( false ) // certs != null )
            // {
            // result.getRequestContext().put( CertificateBasedAccessDecisionVoter.REQUEST_CERTIFICATES, certs );
            // }
        }
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
            null,
            groupId,
            artifactId,
            version,
            null,
            null,
            null );

        try
        {
            Repository repository = getNexus().getRepository( repositoryId );

            if ( !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );
            }

            InputStream pomContent = null;

            InputStreamReader ir = null;

            Model pom = null;

            try
            {
                ArtifactStoreHelper helper = new ArtifactStoreHelper( (MavenRepository) repository );

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

        ArtifactStoreRequest gavRequest = getResourceStoreRequest(
            request,
            false,
            repositoryId,
            null,
            groupId,
            artifactId,
            version,
            packaging,
            classifier,
            extension );

        try
        {
            Repository repository = getNexus().getRepository( repositoryId );

            if ( !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );
            }

            ArtifactStoreHelper helper = new ArtifactStoreHelper( (MavenRepository) repository );

            StorageFileItem file = helper.retrieveArtifact( gavRequest );

            if ( redirectTo )
            {
                String filePath = file.getRepositoryItemUid().getPath();

                if ( filePath.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
                {
                    filePath = filePath.substring( 1 );
                }

                filePath = "content/" + filePath;

                Reference repoRoot = createRepositoryReference( request, file
                    .getRepositoryItemUid().getRepository().getId() );

                Reference fileReference = createReference( repoRoot, filePath );

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
        try
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

                    ArtifactStoreRequest gavRequest;

                    if ( hasPom )
                    {
                        if ( isPom )
                        {
                            pomManager.storeTempPomFile( fi.getInputStream() );

                            is = pomManager.getTempPomFileInputStream();

                        }
                        else
                        {
                            is = fi.getInputStream();
                        }

                        // this is ugly: since GAVRequest does not allow contructing
                        // without GAV, i am filling it with dummy values, and pomManager
                        // will set those to proper values
                        try
                        {
                            gavRequest = pomManager.getGAVRequestFromTempPomFile( getResourceStoreRequest(
                                request,
                                true,
                                repositoryId,
                                null,
                                "G",
                                "A",
                                "V",
                                "P",
                                null,
                                null ) );
                        }
                        catch ( IOException e )
                        {
                            getLogger().info( e.getMessage() );

                            throw new ResourceException(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Error occurred while reading the POM file. Malformed POM?" );
                        }

                        if ( !isPom )
                        {

                            // Can't retrieve these details from the pom, so we must expect the user to provide them
                            // If now, the classifier will not be appended, and we will use the extension mapped from
                            // the packaging type in the pom (or the packaging type provided
                            if ( !StringUtils.isEmpty( extension ) )
                            {
                                gavRequest.setExtension( extension );
                            }

                            if ( !StringUtils.isEmpty( classifier ) )
                            {
                                gavRequest.setClassifier( classifier );
                            }
                        }
                    }
                    else
                    {
                        is = fi.getInputStream();

                        gavRequest = getResourceStoreRequest(
                            request,
                            true,
                            repositoryId,
                            null,
                            groupId,
                            artifactId,
                            version,
                            packaging,
                            classifier,
                            extension );
                    }

                    try
                    {
                        Repository repository = getNexus().getRepository( repositoryId );

                        ArtifactStoreHelper helper = new ArtifactStoreHelper( (MavenRepository) repository );

                        if ( !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
                        {
                            getLogger().warn( "Upload to non maven repository attempted" );
                            throw new ResourceException(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "This is not a Maven repository!" );
                        }
                        else
                        {
                            // temporarily we disable SNAPSHOT upload
                            // check is it a Snapshot repo
                            MavenRepository mr = (MavenRepository) repository;

                            if ( RepositoryPolicy.SNAPSHOT.equals( mr.getRepositoryPolicy() ) )
                            {
                                getLogger().info( "Upload to SNAPSHOT maven repository attempted" );

                                throw new ResourceException(
                                    Status.CLIENT_ERROR_BAD_REQUEST,
                                    "This is a Maven SNAPSHOT repository, and manual upload against it is forbidden!" );
                            }
                        }

                        if ( !versionMatchesPolicy( gavRequest.getVersion(), ( (MavenRepository) repository )
                            .getRepositoryPolicy() ) )
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
                                helper.storeArtifactWithGeneratedPom( gavRequest, is, null );
                            }
                        }
                    }
                    catch ( IllegalArgumentException e )
                    {
                        getLogger().info( "Cannot upload!", e );

                        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
                    }
                }
            }

            if ( hasPom )
            {
                pomManager.removeTempPomFile();
            }
        }
        catch ( Exception e )
        {
            handleException( request, response, e );
        }

        return null;
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
        else if ( t instanceof RepositoryNotListableException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
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
            // TODO: a big fat problem here!
            // this makes restlet code tied to Servlet code, and we what is happening here is VERY dirty!
            HttpServletRequest servletRequest = ( (ServletCall) ( (HttpRequest) request ).getHttpCall() ).getRequest();

            String scheme = (String) servletRequest.getAttribute( NexusHttpAuthenticationFilter.AUTH_SCHEME_KEY );

            ChallengeScheme challengeScheme = null;

            if ( NexusHttpAuthenticationFilter.FAKE_AUTH_SCHEME.equals( scheme ) )
            {
                challengeScheme = new ChallengeScheme( "HTTP_NXBASIC", "NxBasic", "Fake basic HTTP authentication" );
            }
            else
            {
                challengeScheme = ChallengeScheme.HTTP_BASIC;
            }

            String realm = (String) servletRequest.getAttribute( NexusHttpAuthenticationFilter.AUTH_REALM_KEY );

            res.setStatus( Status.CLIENT_ERROR_UNAUTHORIZED );

            res.getChallengeRequests().add( new ChallengeRequest( challengeScheme, realm ) );

            // throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED, "Authenticate to access this resource!" );
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

    private boolean versionMatchesPolicy( String version, RepositoryPolicy policy )
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
