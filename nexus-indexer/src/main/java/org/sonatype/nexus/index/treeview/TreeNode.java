package org.sonatype.nexus.index.treeview;

import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;

public interface TreeNode
{
    boolean isLeaf();

    void setLeaf( boolean leaf );

    String getNodeName();

    void setNodeName( String name );

    String getPath();

    void setPath( String path );

    List<TreeNode> getChildren();

    List<TreeNode> listChildren()
        throws IndexContextInInconsistentStateException,
            IOException;
}
