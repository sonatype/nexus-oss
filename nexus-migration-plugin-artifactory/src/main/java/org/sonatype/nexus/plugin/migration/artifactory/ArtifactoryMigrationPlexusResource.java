package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryProxy;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;
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

                System.out.println( fileItem.getName() );
                System.out.println( Arrays.toString( artifactoryBackup.listFiles() ) );

                File configFile = new File( artifactoryBackup, "artifactory.config.xml" );

                ArtifactoryConfig cfg = ArtifactoryConfig.read( configFile );

                validate( cfg.getLocalRepositories(), cfg.getRemoteRepositories() );

                importRepositories( cfg.getLocalRepositories(), cfg.getProxies() );
                importRepositories( cfg.getRemoteRepositories(), cfg.getProxies() );

                importGroups( cfg.getVirtualRepositories() );

            }
            catch ( Exception e )
            {
                e.printStackTrace();
                getLogger().error( "Unable to extract backup content", e );
                throw new ResourceException( e );
            }
        }
        return null;
    }

    private void importGroups( List<ArtifactoryVirtualRepository> virtualRepositories )
        throws ResourceException
    {
        for ( ArtifactoryVirtualRepository virtualRepo : virtualRepositories )
        {
            CRepositoryGroup group = new CRepositoryGroup();
            group.setGroupId( virtualRepo.getKey() );
            group.setName( virtualRepo.getKey() );

            for ( String repo : virtualRepo.getRepositories() )
            {
                group.addRepository( repo );
            }

            try
            {
                getNexus().createRepositoryGroup( group );
            }
            catch ( Exception e )
            {
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to create group "
                    + virtualRepo.getKey(), e );
            }
        }
    }

    private void importRepositories( List<ArtifactoryRepository> repositories, Map<String, ArtifactoryProxy> proxies )
        throws ResourceException
    {
        for ( ArtifactoryRepository repo : repositories )
        {
            CRepository nexusRepo = new CRepository();
            nexusRepo.setId( repo.getKey() );
            nexusRepo.setName( repo.getDescription() );
            if ( repo.getHandleSnapshots() )
            {
                nexusRepo.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );
            }

            String url = repo.getUrl();
            if ( url != null )
            {
                CRemoteStorage remote = new CRemoteStorage();
                remote.setUrl( url );

                String proxyId = repo.getProxy();
                if ( proxyId != null )
                {
                    ArtifactoryProxy proxy = proxies.get( proxyId );

                    CRemoteHttpProxySettings nexusProxy = new CRemoteHttpProxySettings();
                    nexusProxy.setProxyHostname( proxy.getHost() );
                    nexusProxy.setProxyPort( proxy.getPort() );

                    CRemoteAuthentication authentication = new CRemoteAuthentication();
                    authentication.setUsername( proxy.getUsername() );
                    authentication.setPassword( proxy.getPassword() );
                    nexusProxy.setAuthentication( authentication );

                    remote.setHttpProxySettings( nexusProxy );
                }
                nexusRepo.setRemoteStorage( remote );
            }

            try
            {
                getNexus().createRepository( nexusRepo );
            }
            catch ( Exception e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Error creating repository "
                    + repo.getKey() );
            }
        }

    }

    private void validate( List<ArtifactoryRepository>... repositoriesSet )
        throws ResourceException
    {
        for ( List<ArtifactoryRepository> repositories : repositoriesSet )
        {
            for ( ArtifactoryRepository repository : repositories )
            {
                if ( repository.getHandleReleases() && repository.getHandleSnapshots() )
                {
                    // TODO need to ask for user for the correct handle on that case
                    throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                                 "Repositories with releases and snapshots are not supported. Yet." );
                }

            }
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
