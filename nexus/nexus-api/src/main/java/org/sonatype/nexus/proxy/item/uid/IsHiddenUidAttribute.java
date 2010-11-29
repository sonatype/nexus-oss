package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class IsHiddenUidAttribute
    implements Attribute<Boolean>
{
    public Boolean getValueFor( final RepositoryItemUid subject )
    {
        // paths that start with a . in any directory (or filename)
        // are considered hidden.
        // This check will catch (for example):
        // .metadata
        // /.meta/something.jar
        // /something/else/.hidden/something.jar
        if ( subject.getPath() != null
            && ( subject.getPath().indexOf( "/." ) > -1 || subject.getPath().startsWith( "." ) ) )
        {
            return Boolean.TRUE;
        }
        else
        {
            return Boolean.FALSE;
        }
    }
}
