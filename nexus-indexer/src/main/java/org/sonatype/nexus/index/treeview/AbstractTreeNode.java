package org.sonatype.nexus.index.treeview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;

public abstract class AbstractTreeNode
    implements TreeNode
{
    private boolean leaf;

    private String nodeName;

    private String path;

    private List<TreeNode> children;

    private transient final IndexTreeView treeView;

    private transient final TreeNodeFactory factory;

    public AbstractTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        this.treeView = tview;

        this.factory = factory;
    }

    public boolean isLeaf()
    {
        return leaf;
    }

    public void setLeaf( boolean leaf )
    {
        this.leaf = leaf;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName( String nodeName )
    {
        this.nodeName = nodeName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public List<TreeNode> getChildren()
    {
        if ( children == null && !isLeaf() )
        {
            children = new ArrayList<TreeNode>();
        }

        return children;
    }

    public List<TreeNode> listChildren()
        throws IndexContextInInconsistentStateException,
            IOException
    {
        if ( !isLeaf() && getChildren().isEmpty() && !isLeaf() )
        {
            children = treeView.listNodes( factory, getPath() ).getChildren();
        }

        return children;
    }
}
