package org.sonatype.nexus.index;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.wastebasket.AbstractRepositoryFolderCleaner;
import org.sonatype.nexus.proxy.wastebasket.RepositoryFolderCleaner;

@Component( role = RepositoryFolderCleaner.class, hint = "indexer-lucene" )
public class IndexRepositoryFolderCleaner
    extends AbstractRepositoryFolderCleaner
{
    public void cleanRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return;
        }

        File indexerFolder =
            getApplicationConfiguration().getWorkingDirectory( DefaultIndexerManager.INDEXER_WORKING_DIRECTORY_KEY );

        delete( new File( indexerFolder, repository.getId() + "-local" ), deleteForever );

        delete( new File( indexerFolder, repository.getId() + "-remote" ), deleteForever );
    }

}
