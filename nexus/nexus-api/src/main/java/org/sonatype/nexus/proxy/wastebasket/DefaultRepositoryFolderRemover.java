package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = RepositoryFolderRemover.class )
public class DefaultRepositoryFolderRemover
    implements RepositoryFolderRemover
{
    @Requirement
    private Logger logger;

    @Requirement( role = RepositoryFolderCleaner.class )
    private Map<String, RepositoryFolderCleaner> cleaners;

    protected Logger getLogger()
    {
        return logger;
    }

    public void deleteRepositoryFolders( final Repository repository, final boolean deleteForever )
        throws IOException
    {
        getLogger().debug(
            "Removing folders of repository \"" + repository.getName() + "\" (ID=" + repository.getId() + ")" );

        for ( RepositoryFolderCleaner cleaner : cleaners.values() )
        {
            try
            {
                cleaner.cleanRepositoryFolders( repository, deleteForever );
            }
            catch ( Exception e )
            {
                getLogger().warn(
                    "Got exception during execution of RepositoryFolderCleaner " + cleaner.getClass().getName()
                        + ", continuing.", e );
            }
        }
    }
}
