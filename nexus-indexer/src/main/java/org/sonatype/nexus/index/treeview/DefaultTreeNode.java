package org.sonatype.nexus.index.treeview;

/**
 * The simplest treenode, that does not adds any "decoration" to the nodes.
 * 
 * @author cstamas
 */
public class DefaultTreeNode
    extends AbstractTreeNode
{
    public DefaultTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }
}
