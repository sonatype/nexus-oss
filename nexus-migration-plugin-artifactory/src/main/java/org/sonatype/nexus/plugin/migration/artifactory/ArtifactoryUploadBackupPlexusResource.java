package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryMigrationUpload" )
public class ArtifactoryUploadBackupPlexusResource
    extends AbstractArtifactoryMigrationPlexusResource

{

    @Requirement( role = org.codehaus.plexus.archiver.UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    public ArtifactoryUploadBackupPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
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
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:migration]" );
    }

    @Override
    public boolean acceptsUpload()
    {
        return true;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {

        for ( FileItem fileItem : files )
        {
            try
            {
                File artifactoryBackup = saveArtifactoryBackup( fileItem );

                File configFile = new File( artifactoryBackup, "artifactory.config.xml" );

                ArtifactoryConfig cfg = ArtifactoryConfig.read( configFile );

                MigrationSummaryDTO data = new MigrationSummaryDTO();
                data.setBackupLocation( artifactoryBackup.getAbsolutePath() );

                List<RepositoryResolutionDTO> repositoriesResolution =
                    validate( cfg.getLocalRepositories().values(), cfg.getRemoteRepositories().values() );
                data.setRepositoriesResolution( repositoriesResolution );

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

    private List<RepositoryResolutionDTO> validate( Collection<ArtifactoryRepository>... repositoriesSet )
        throws ResourceException
    {
        List<RepositoryResolutionDTO> resolutions = new ArrayList<RepositoryResolutionDTO>();
        for ( Collection<ArtifactoryRepository> artifactoryRepos : repositoriesSet )
        {
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
        }
        return resolutions;
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
            if ( !RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
            {
                continue;
            }

            if ( url.equals( repository.getRemoteUrl() ) )
            {
                return repository.getId();
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
}
