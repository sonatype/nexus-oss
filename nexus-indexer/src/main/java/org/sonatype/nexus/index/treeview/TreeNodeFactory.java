package org.sonatype.nexus.index.treeview;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;

public interface TreeNodeFactory
{
    IndexingContext getIndexingContext();

    TreeNode createNode( IndexTreeView tview, String path, String name );

    TreeNode createANode( IndexTreeView tview, ArtifactInfo ai, String path );

    TreeNode createVNode( IndexTreeView tview, ArtifactInfo ai, String path );

    TreeNode createArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path );
}
