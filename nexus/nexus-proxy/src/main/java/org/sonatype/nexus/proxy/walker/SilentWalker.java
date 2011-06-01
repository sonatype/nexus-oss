package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;

/**
 * SilentWalker only exists to enforce that classes like WastebasketWalker won't throw exceptions
 * 
 * @author Marvin Froeder
 */
public interface SilentWalker
{
    void processItem( WalkerContext ctx, StorageItem item );
}
