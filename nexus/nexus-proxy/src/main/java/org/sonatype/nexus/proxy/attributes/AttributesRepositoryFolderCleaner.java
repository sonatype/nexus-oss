package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.wastebasket.AbstractRepositoryFolderCleaner;
import org.sonatype.nexus.proxy.wastebasket.RepositoryFolderCleaner;

/**
 * TO BE REMOVED once we switch from FS based attribute storage to LS based attribute storage!
 * 
 * @author cstamas
 */
@Component( role = RepositoryFolderCleaner.class, hint = "core-proxy-attributes" )
public class AttributesRepositoryFolderCleaner
    extends AbstractRepositoryFolderCleaner
{

    @Override
    public void cleanRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException
    {
        File defaultProxyAttributesFolder =
            new File( new File( getApplicationConfiguration().getWorkingDirectory(), "proxy/attributes" ),
                repository.getId() );
        
        if ( defaultProxyAttributesFolder.isDirectory() )
        {
            // attributes are not preserved
            delete( defaultProxyAttributesFolder, true );
        }
    }

}
