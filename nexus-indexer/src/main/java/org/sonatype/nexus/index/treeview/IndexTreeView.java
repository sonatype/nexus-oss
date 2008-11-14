package org.sonatype.nexus.index.treeview;

import java.io.IOException;

import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;

public interface IndexTreeView
{
    TreeNode listNodes( TreeNodeFactory factory, String path )
        throws IndexContextInInconsistentStateException,
            IOException;
}
