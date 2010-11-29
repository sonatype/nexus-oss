package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

@Component( role = RepositoryFolderRemover.class )
public class DefaultRepositoryFolderRemover
    implements RepositoryFolderRemover
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    private File wastebasketDirectory;

    protected File getWastebasketDirectory()
    {
        synchronized ( this )
        {
            if ( wastebasketDirectory == null )
            {
                wastebasketDirectory = applicationConfiguration.getWastebasketDirectory();

                wastebasketDirectory.mkdirs();
            }

            return wastebasketDirectory;
        }
    }

    public void deleteRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException
    {
        deleteStorage( repository, deleteForever );

        deleteProxyAttributes( repository, true );

        deleteIndexer( repository, true );
    }

    protected void deleteStorage( Repository repository, boolean deleteForever )
        throws IOException
    {
        File defaultStorageFolder =
            new File( new File( applicationConfiguration.getWorkingDirectory(), "storage" ), repository.getId() );

        String defaultStorageURI = defaultStorageFolder.toURI().toURL().toString();
        defaultStorageURI = defaultStorageURI.endsWith( "/" ) ? defaultStorageURI : defaultStorageURI + "/";

        String localURI = repository.getLocalUrl();
        localURI = localURI.endsWith( "/" ) ? localURI : localURI + "/";

        boolean sameLocation = defaultStorageURI.equals( localURI );

        if ( sameLocation )
        {
            delete( defaultStorageFolder, deleteForever );
        }
    }

    protected void deleteProxyAttributes( Repository repository, boolean deleteForever )
        throws IOException
    {
        File proxyAttributesFolder =
            new File( new File( new File( applicationConfiguration.getWorkingDirectory(), "proxy" ), "attributes" ),
                repository.getId() );

        delete( proxyAttributesFolder, true );
    }

    protected void deleteIndexer( Repository repository, boolean deleteForever )
        throws IOException
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return;
        }

        File indexerFolder = new File( applicationConfiguration.getWorkingDirectory(), "indexer" );

        delete( new File( indexerFolder, repository.getId() + "-local" ), deleteForever );

        delete( new File( indexerFolder, repository.getId() + "-remote" ), deleteForever );
    }

    /**
     * Move the file to trash, or simply delete it forever
     * 
     * @param file file to be deleted
     * @param deleteForever if it's true, delete the file forever, if it's false, move the file to trash
     * @throws IOException
     */
    protected void delete( File file, boolean deleteForever )
        throws IOException
    {
        if ( !deleteForever )
        {
            File basketFile = new File( getWastebasketDirectory(), file.getName() );

            if ( file.isDirectory() )
            {
                FileUtils.mkdir( basketFile.getAbsolutePath() );

                FileUtils.copyDirectoryStructure( file, basketFile );
            }
            else
            {
                FileUtils.copyFile( file, basketFile );
            }
        }

        FileUtils.forceDelete( file );
    }

}
