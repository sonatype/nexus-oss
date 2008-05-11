package org.sonatype.nexus.proxy.maven;

import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.RecreateAttributesWalker;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;

public class RecreateMavenAttributesWalker
    extends RecreateAttributesWalker
{
    private boolean shouldFixChecksums;

    public RecreateMavenAttributesWalker( Repository repository, Logger logger, Map<String, String> initialData )
    {
        super( repository, logger, initialData );

        this.shouldFixChecksums = RepositoryType.HOSTED.equals( getRepository().getRepositoryType() );
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        super.processFileItem( item );

        if ( shouldFixChecksums && !item.getName().endsWith( ".sha1" ) && !item.getName().endsWith( ".md5" ) )
        {
            // fix checksums here:
            // the fileitem we process is not checksum file, hence we should check for the existince of it's checksums,
            // and if not exists, create it
        }
    }
}
