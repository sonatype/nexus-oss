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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipFile;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationResource;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryUser;
import org.sonatype.nexus.plugin.migration.artifactory.security.builder.ArtifactorySecurityConfigBuilder;
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryFileLocation" )
public class ArtifactoryFileLocationPlexusResource
    extends AbstractArtifactoryMigrationPlexusResource
{
    private static final String SECURITY_FILE = "security.xml";

    private static final String ARTIFACTORY_CONF_FILE = "artifactory.config.xml";

    @Requirement
    private NexusSecurity nexusSecurity;

    @Requirement
    private Logger logger;

    public ArtifactoryFileLocationPlexusResource()
    {
        this.setModifiable( true );
    }

    protected NexusSecurity getNexusSecurity()
    {
        return nexusSecurity;
    }

    @Override
    public Object getPayloadInstance()
    {
        return new FileLocationRequestDTO();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:artifactoryfilelocation]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/migration/artifactory/filelocation";
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        String fileLocation = retrieveFileLocation( (FileLocationRequestDTO) payload );

        File backupFile = validateBackupFileLocation( fileLocation );

        ZipFile zipFile = null;

        // the return payload
        MigrationSummaryResponseDTO result = new MigrationSummaryResponseDTO();

        try
        {
            // stream files out of the zip, we don't have time to unzip the file first.
            zipFile = new ZipFile( backupFile );

            ZipEntry artifactoryConfEntry = zipFile.getEntry( ARTIFACTORY_CONF_FILE );
            if ( artifactoryConfEntry == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                             "Artifactory backup is invalid, missing: '" + ARTIFACTORY_CONF_FILE + "'" );
            }

            ZipEntry securityEntry = zipFile.getEntry( SECURITY_FILE );
            if ( securityEntry == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                             "Artifactory backup is invalid, missing: '" + SECURITY_FILE + "'" );
            }

            MigrationSummaryDTO data = new MigrationSummaryDTO();
            data.setId( Long.toHexString( System.nanoTime() ) );

            result.setData( data );

            buildBackupLoaction( backupFile, data );

            buildArtifactoryConfig( zipFile.getInputStream( artifactoryConfEntry ), data );

            buildSecurityConfig( zipFile.getInputStream( securityEntry ), data );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        finally
        {
            try
            {
                if ( zipFile != null )
                {
                    zipFile.close();
                }
            }
            catch ( IOException e )
            {
                this.logger.debug( "Exception closing Artifactory zip file: " + e.getMessage(), e );
            }
        }

        return result;
    }

    private void buildBackupLoaction( File backupFile, MigrationSummaryDTO data )
    {
        data.setBackupLocation( backupFile.getAbsolutePath() );
    }

    private void buildArtifactoryConfig( InputStream stream, MigrationSummaryDTO data )
        throws ResourceException, IOException, XmlPullParserException
    {
        // read artifactory.config.xml
        ArtifactoryConfig cfg = ArtifactoryConfig.read( stream );

        final Map<String, ArtifactoryRepository> repositories = cfg.getRepositories();

        List<RepositoryResolutionDTO> repositoriesResolution = resolve( repositories.values() );

        data.setRepositoriesResolution( repositoriesResolution );

        List<GroupResolutionDTO> groupsResolution = resolve( cfg.getVirtualRepositories(), repositories );

        data.setGroupsResolution( groupsResolution );
    }

    private void buildSecurityConfig( InputStream stream, MigrationSummaryDTO data )
        throws IOException, XmlPullParserException
    {
        // read security.xml
        ArtifactorySecurityConfig securityCfg = ArtifactorySecurityConfigBuilder.read( stream );

        List<UserResolutionDTO> userResolution = resolve( securityCfg.getUsers() );

        data.setUsersResolution( userResolution );
    }

    private String retrieveFileLocation( FileLocationRequestDTO fileLocationRequest )
        throws ResourceException
    {
        if ( fileLocationRequest == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Empty Request." );
        }

        FileLocationResource data = fileLocationRequest.getData();

        if ( data == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Empty Request Data." );
        }

        String fileLocation = data.getFileLocation();

        if ( StringUtils.isEmpty( fileLocation ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Empty File Location." );
        }

        return fileLocation;
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
            RepositoryResolutionDTO resolution = new RepositoryResolutionDTO();
            String repoId = repoArtifactory.getKey();
            resolution.setRepositoryId( repoId );

            ERepositoryType type = ERepositoryType.HOSTED;
            String similarId = null;

            if ( repoArtifactory.getUrl() != null )
            {
                type = ERepositoryType.PROXIED;
                similarId = findSimilarRepository( repoArtifactory.getUrl() );
            }

            resolution.setType( type );
            resolution.setSimilarRepositoryId( similarId );

            Repository nexusRepo = getRepository( repoId );
            if ( nexusRepo != null )
            {
                resolution.setAlreadyExists( true );
            }
            else
            {
                if ( repoArtifactory.getHandleReleases() && repoArtifactory.getHandleSnapshots() )
                {
                    resolution.setMixed( true );
                }
            }

            resolutions.add( resolution );

        }
        return resolutions;
    }

    private Repository getRepository( String repoId )
    {
        try
        {
            return getNexus().getRepository( repoId );
        }
        catch ( NoSuchRepositoryException e )
        {
            // that is not a problem
            return null;
        }
    }

    private List<UserResolutionDTO> resolve( List<ArtifactoryUser> users )
    {
        List<UserResolutionDTO> resolutions = new ArrayList<UserResolutionDTO>( users.size() );

        for ( ArtifactoryUser user : users )
        {
            UserResolutionDTO resolution = new UserResolutionDTO();

            validateUser( user );

            resolution.setUserId( user.getUsername() );

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

}
