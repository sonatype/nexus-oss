package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.config.CentralConfig;
import org.artifactory.config.jaxb.JaxbHelper;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RemoteRepo;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryMigration" )
public class ArtifactoryMigrationPlexusResource
    extends AbstractNexusPlexusResource

{

    @Requirement( role = org.codehaus.plexus.archiver.UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    public ArtifactoryMigrationPlexusResource()
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
        return "/migration/artifactory/content";
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

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {

        for ( FileItem fileItem : files )
        {
            try
            {
                File artifactoryBackup = saveArtifactoryBackup( fileItem );

                System.out.println( fileItem.getName() );
                System.out.println( Arrays.toString( artifactoryBackup.listFiles() ) );

                File configFile = new File( artifactoryBackup, "artifactory.config.xml" );

                CentralConfig artifactoryConfig =
                    new JaxbHelper<CentralConfig>().read( configFile.getAbsolutePath(), CentralConfig.class );

                Map<String, LocalRepo> localRepositoriesMap = get( artifactoryConfig, "localRepositoriesMap" );
                Map<String, RemoteRepo> remoteRepositoriesMap = get( artifactoryConfig, "remoteRepositoriesMap" );

                validate( localRepositoriesMap, remoteRepositoriesMap );

                for ( LocalRepo repo : localRepositoriesMap.values() )
                {
                    CRepository nexusRepo = new CRepository();
                    nexusRepo.setId( repo.getKey() );
                    nexusRepo.setName( repo.getDescription() );
                    if ( repo.isHandleReleases() )
                    {
                        nexusRepo.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_RELEASE );
                    }
                    else if ( repo.isHandleSnapshots() )
                    {
                        nexusRepo.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );
                    }
                    else
                    {
                        // TODO
                    }
                    getNexus().createRepository( nexusRepo );
                }

            }
            catch ( Exception e )
            {
                e.printStackTrace();
                getLogger().error( "Unable to extract backup content", e );
                throw new ResourceException( e );
            }
        }
        return super.upload( context, request, response, files );
    }

    private void validate( Map<String, LocalRepo> localRepositoriesMap, Map<String, RemoteRepo> remoteRepositoriesMap )
        throws ResourceException
    {
        validate( localRepositoriesMap.values() );
        validate( remoteRepositoriesMap.values() );
    }

    private void validate( Collection<? extends RealRepo> repos )
        throws ResourceException
    {
        for ( RealRepo repo : repos )
        {
            if ( repo.isHandleReleases() && repo.isHandleSnapshots() )
            {
                // TODO need to ask for user for the correct handle on that case
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                             "Repositories with releases and snapshots are not supported. Yet." );
            }
        }

    }

    @SuppressWarnings( "unchecked" )
    private <E> E get( CentralConfig artifactoryConfig, String fieldName )
        throws ResourceException
    {
        try
        {
            Field field = CentralConfig.class.getDeclaredField( fieldName );
            field.setAccessible( true );

            return (E) field.get( artifactoryConfig );
        }
        catch ( Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to retrieve '" + fieldName
                + "' from Artifactory configuration.", e );
        }
    }

    private File saveArtifactoryBackup( FileItem fileItem )
        throws ResourceException
    {
        File tempDir = getNexus().getNexusConfiguration().getTemporaryDirectory();

        File artifactoryBackupZip = new File( tempDir, fileItem.getName() );

        try
        {
            InputStream in = fileItem.getInputStream();
            OutputStream out = new FileOutputStream( artifactoryBackupZip );

            IOUtils.copy( in, out );

            in.close();
            out.close();

            File artifactoryBackup = new File( tempDir, FilenameUtils.getBaseName( fileItem.getName() ) );
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
