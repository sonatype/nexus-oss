package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class HiddenUid
    implements Attribute<Boolean>
{
    private final RepositoryItemUid uid;

    public HiddenUid( RepositoryItemUid uid )
    {
        this.uid = uid;
    }

    public Boolean getValue()
    {
        // paths that start with a . in any directory (or filename)
        // are considered hidden.
        // This check will catch (for example):
        // .metadata
        // /.meta/something.jar
        // /something/else/.hidden/something.jar
        if ( uid.getPath() != null && ( uid.getPath().indexOf( "/." ) > -1 || uid.getPath().startsWith( "." ) ) )
        {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
