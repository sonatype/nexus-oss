package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = RepositoryFolderCleaner.class, hint = "core-storage" )
public class CoreRepositoryFolderCleaner
    extends AbstractRepositoryFolderCleaner
{
    public void cleanRepositoryFolders( final Repository repository, boolean deleteForever )
        throws IOException
    {
        File defaultStorageFolder =
            new File( new File( getApplicationConfiguration().getWorkingDirectory(), "storage" ), repository.getId() );

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
}
