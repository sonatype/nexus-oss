package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
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
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryMigration" )
public class ArtifactoryMigrationPlexusResource
    extends AbstractNexusPlexusResource

{

    @Requirement( role = org.codehaus.plexus.archiver.UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    @Requirement
    private NexusScheduler nexusScheduler;

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

                File repositoriesBackup = new File( artifactoryBackup, "repositories" );
                importRepositories( cfg.getLocalRepositories(), cfg.getProxies(), repositoriesBackup );
                importRepositories( cfg.getRemoteRepositories(), cfg.getProxies(), repositoriesBackup );

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

    private void importRepositories( List<ArtifactoryRepository> repositories, Map<String, ArtifactoryProxy> proxies,
                                     File repositoriesBackup )
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

            File repositoryBackup = new File( repositoriesBackup, repo.getKey() );

            if ( repositoryBackup.isDirectory() && repositoryBackup.list().length > 0 )
            {
                copyArtifacts( nexusRepo, repositoryBackup );

                ReindexTask rt = (ReindexTask) nexusScheduler.createTaskInstance( ReindexTaskDescriptor.ID );
                rt.setRepositoryId( nexusRepo.getId() );
                nexusScheduler.submit( "Download remote index enabled.", rt );

            }

        }

    }

    private void copyArtifacts( CRepository nexusRepo, File repositoryBackup )
        throws ResourceException
    {
        try
        {
            File storage = new File( getStorage( nexusRepo ).toURI() );

            // filter artifactory metadata
            NotFileFilter filter = new NotFileFilter( new SuffixFileFilter( ".artifactory-metadata" ) );
            FileUtils.copyDirectory( repositoryBackup, storage, filter );
        }
        catch ( Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to copy artifacts to "
                + nexusRepo.getId(), e );
        }
    }

    private URL getStorage( CRepository nexusRepo )
        throws ResourceException, MalformedURLException
    {
        URL storage;
        if ( nexusRepo.getLocalStorage() == null )
        {
            storage = new URL( nexusRepo.defaultLocalStorageUrl );
        }
        else
        {
            storage = new URL( nexusRepo.getLocalStorage().getUrl() );
        }
        return storage;
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
