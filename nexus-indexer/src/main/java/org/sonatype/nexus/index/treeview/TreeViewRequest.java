package org.sonatype.nexus.index.treeview;

public class TreeViewRequest
{
    private final TreeNodeFactory factory;

    private final String path;

    public TreeViewRequest( TreeNodeFactory factory, String path )
    {
        this.factory = factory;

        this.path = path;
    }

    public TreeNodeFactory getFactory()
    {
        return factory;
    }

    public String getPath()
    {
        return path;
    }
}
