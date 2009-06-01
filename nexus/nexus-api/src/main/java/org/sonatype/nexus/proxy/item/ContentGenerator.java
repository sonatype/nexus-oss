package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.plugin.ExtensionPoint;

/**
 * A content generator is a special component, that is able to generate content on-the-fly, that will be substituted
 * with the content coming from the Local Storage.
 * 
 * @author cstamas
 */
@ExtensionPoint
public interface ContentGenerator
{
    public static final String CONTENT_GENERATOR_ID = "contentGenerator";

    ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException;
}
