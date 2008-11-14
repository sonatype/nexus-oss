package org.sonatype.nexus.index.treeview;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;

public class SimpleTreeNodeFactory
    implements TreeNodeFactory
{
    private IndexingContext context;

    public SimpleTreeNodeFactory( IndexingContext ctx )
    {
        this.context = ctx;
    }

    public IndexingContext getIndexingContext()
    {
        return context;
    }

    public TreeNode createNode( IndexTreeView tview, String path, String groupName )
    {
        SimpleTreeNode result = createNode( tview, path, false, groupName );

        return result;
    }

    public TreeNode createANode( IndexTreeView tview, ArtifactInfo ai, String path )
    {
        SimpleTreeNode result = createNode( tview, path, false, ai.artifactId );

        result.setGroupId( ai.groupId );

        result.setArtifactId( ai.artifactId );

        return result;
    }

    public TreeNode createVNode( IndexTreeView tview, ArtifactInfo ai, String path )
    {
        SimpleTreeNode result = createNode( tview, path, false, ai.version );

        result.setGroupId( ai.groupId );

        result.setArtifactId( ai.artifactId );

        result.setVersion( ai.version );

        return result;
    }

    public TreeNode createArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path )
    {
        StringBuffer sb = new StringBuffer( ai.artifactId ).append( "-" ).append( ai.version );

        if ( ai.classifier != null )
        {
            sb.append( "-" ).append( ai.classifier );
        }

        sb.append( "." ).append( ai.fextension == null ? "jar" : ai.fextension );

        SimpleTreeNode result = createNode( tview, path, true, sb.toString() );

        result.setGroupId( ai.groupId );

        result.setArtifactId( ai.artifactId );

        result.setVersion( ai.version );

        return result;
    }

    protected SimpleTreeNode createNode( IndexTreeView tview, String path, boolean leaf, String nodeName )
    {
        SimpleTreeNode result = new SimpleTreeNode( tview, this );

        result.setPath( path );

        result.setLeaf( leaf );

        result.setNodeName( nodeName );

        return result;
    }

}
