package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class IsMetadataMaintainedAttribute
    implements Attribute<Boolean>
{
    public Boolean getValueFor( RepositoryItemUid subject )
    {
        // stuff not having metadata:
        // /.nexus/attributes
        // /.nexus/trash

        if ( subject.getPath() != null )
        {
            // TODO: how to avoid path duplication?
            return !( subject.getPath().startsWith( "/.nexus/attributes" ) || subject.getPath().startsWith(
                "/.nexus/trash" ) );
        }
        else
        {
            return true;
        }
    }
}
