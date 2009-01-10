package org.sonatype.nexus.proxy;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class InvalidItemContentException
    extends StorageException
{
    private static final long serialVersionUID = -1749678254941504279L;

    public InvalidItemContentException( String msg, Throwable cause )
    {
        super( msg, cause );
    }

    public InvalidItemContentException( RepositoryItemUid uid )
    {
        super( "Item content is invalid" );
    }

}
