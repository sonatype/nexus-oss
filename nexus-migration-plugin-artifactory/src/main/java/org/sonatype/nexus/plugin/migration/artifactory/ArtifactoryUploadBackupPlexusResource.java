/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryUser;
import org.sonatype.nexus.plugin.migration.artifactory.security.builder.ArtifactorySecurityConfigBuilder;
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryMigrationUpload" )
public class ArtifactoryUploadBackupPlexusResource
    extends AbstractArtifactoryMigrationPlexusResource

{

    @Requirement( role = org.codehaus.plexus.archiver.UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    @Requirement
    private NexusSecurity nexusSecurity;

    public ArtifactoryUploadBackupPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    protected NexusSecurity getNexusSecurity()
    {
        return nexusSecurity;
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/migration/artifactory/upload";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:artifactoryupload]" );
    }

    @Override
    public boolean acceptsUpload()
    {
        return true;
    }

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {

        for ( FileItem fileItem : files )
        {
            try
            {
                File artifactoryBackup = saveArtifactoryBackup( fileItem );

                MigrationSummaryDTO data = new MigrationSummaryDTO();

                data.setBackupLocation( artifactoryBackup.getAbsolutePath() );

                // read artifactory.config.xml
                File configFile = new File( artifactoryBackup, "artifactory.config.xml" );

                ArtifactoryConfig cfg = ArtifactoryConfig.read( configFile );

                final Map<String, ArtifactoryRepository> repositories = cfg.getRepositories();

                List<RepositoryResolutionDTO> repositoriesResolution = resolve( repositories.values() );

                data.setRepositoriesResolution( repositoriesResolution );

                List<GroupResolutionDTO> groupsResolution = resolve( cfg.getVirtualRepositories(), repositories );
                data.setGroupsResolution( groupsResolution );

                // read security.xml
                File securityFile = new File( artifactoryBackup, "security.xml" );

                ArtifactorySecurityConfig securityCfg = ArtifactorySecurityConfigBuilder.read( securityFile );

                List<UserResolutionDTO> userResolution = resolve( securityCfg.getUsers() );

                data.setUserResolution( userResolution );

                // set response
                MigrationSummaryResponseDTO res = new MigrationSummaryResponseDTO();

                res.setData( data );

                return res;
            }
            catch ( ResourceException e )
            {
                throw e;
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                getLogger().error( "Unable to extract backup content", e );
                throw new ResourceException( e );
            }
        }
        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
    }

    private List<GroupResolutionDTO> resolve( Map<String, ArtifactoryVirtualRepository> virtualRepos,
                                              Map<String, ArtifactoryRepository> repositories )
    {
        VirtualRepositoryUtil.resolveRepositories( virtualRepos );

        List<GroupResolutionDTO> groups = new ArrayList<GroupResolutionDTO>();
        for ( ArtifactoryVirtualRepository virtualRepo : virtualRepos.values() )
        {
            List<String> repos = virtualRepo.getResolvedRepositories();
            boolean containsNull = false;
            boolean containsMaven1 = false;
            boolean containsMaven2 = false;
            for ( String repoName : repos )
            {
                ArtifactoryRepository repo = repositories.get( repoName );
                if ( repo.getType() == null )
                {
                    containsNull = true;
                }
                else if ( "maven2".equals( repo.getType() ) )
                {
                    containsMaven2 = true;
                }
                else if ( "maven1".equals( repo.getType() ) )
                {
                    containsMaven1 = true;
                }
            }

            boolean isMixed = ( containsNull || containsMaven2 ) && containsMaven1;

            GroupResolutionDTO group = new GroupResolutionDTO( virtualRepo.getKey(), isMixed );
            groups.add( group );
        }
        return groups;
    }

    private List<RepositoryResolutionDTO> resolve( Collection<ArtifactoryRepository> artifactoryRepos )
        throws ResourceException
    {
        List<RepositoryResolutionDTO> resolutions = new ArrayList<RepositoryResolutionDTO>();
        for ( ArtifactoryRepository repoArtifactory : artifactoryRepos )
        {
            String repoId = repoArtifactory.getKey();
            ERepositoryType type = ERepositoryType.HOSTED;
            String similarId = null;

            if ( repoArtifactory.getUrl() != null )
            {
                type = ERepositoryType.PROXIED;
                similarId = findSimilarRepository( repoArtifactory.getUrl() );
            }

            RepositoryResolutionDTO resolution = new RepositoryResolutionDTO( repoId, type, similarId );

            if ( repoArtifactory.getHandleReleases() && repoArtifactory.getHandleSnapshots() )
            {
                resolution.setMixed( true );
            }

            resolutions.add( resolution );

        }
        return resolutions;
    }

    private List<UserResolutionDTO> resolve( List<ArtifactoryUser> users )
    {
        List<UserResolutionDTO> resolutions = new ArrayList<UserResolutionDTO>( users.size() );

        for ( ArtifactoryUser user : users )
        {
            UserResolutionDTO resolution = new UserResolutionDTO();

            validateUser( user );

            resolution.setId( user.getUsername() );

            resolution.setPassword( user.getPassword() );

            resolution.setAdmin( user.isAdmin() );

            resolution.setEmail( user.getEmail() );

            resolutions.add( resolution );
        }

        return resolutions;
    }

    /**
     * If the user id already exists, append a suffix "-artifactory"
     */
    private void validateUser( ArtifactoryUser artiUser )
    {
        for ( SecurityUser user : getNexusSecurity().listUsers() )
        {
            if ( user.getId().equals( artiUser.getUsername() ) )
            {
                artiUser.setUsername( artiUser.getUsername() + "-artifactory" );
            }
        }
    }

    private String findSimilarRepository( String url )
    {
        if ( url == null )
        {
            return null;
        }

        Collection<Repository> repositories = getNexus().getRepositories();
        for ( Repository repository : repositories )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                ProxyRepository remote = repository.adaptToFacet( ProxyRepository.class );
                if ( url.equals( remote.getRemoteUrl() ) )
                {
                    return repository.getId();
                }
            }

        }
        return null;
    }

    private File saveArtifactoryBackup( FileItem fileItem )
        throws ResourceException
    {
        File tempDir = getNexus().getNexusConfiguration().getTemporaryDirectory();

        try
        {
            File artifactoryBackupZip =
                File.createTempFile( FilenameUtils.getBaseName( fileItem.getName() ), ".zip", tempDir );

            InputStream in = fileItem.getInputStream();
            OutputStream out = new FileOutputStream( artifactoryBackupZip );

            IOUtils.copy( in, out );

            in.close();
            out.close();

            File artifactoryBackup =
                new File( artifactoryBackupZip.getParentFile(),
                          FilenameUtils.getBaseName( artifactoryBackupZip.getAbsolutePath() ) + "content" );
            artifactoryBackup.mkdirs();

            zipUnArchiver.setSourceFile( artifactoryBackupZip );
            zipUnArchiver.setDestDirectory( artifactoryBackup );
            zipUnArchiver.extract();

            return artifactoryBackup;
        }
        catch ( IOException e )
        {
            getLogger().warn( "Unable to retrieve artifactory backup", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }
        catch ( ArchiverException e )
        {
            getLogger().warn( "Unable to extract artifactory backup", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }
    }

    @Override
    public List<Variant> getVariants()
    {
    	List<Variant> variants = new ArrayList<Variant>();

    	//text/html is required for upload response, for ext to handle properly
        variants.add( new Variant( MediaType.TEXT_HTML) );
        variants.add( new Variant( MediaType.APPLICATION_JSON ) );
        variants.add( new Variant( MediaType.APPLICATION_XML ) );
        return variants;
    }
}
