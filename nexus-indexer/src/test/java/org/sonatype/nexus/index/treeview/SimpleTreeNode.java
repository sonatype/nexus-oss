package org.sonatype.nexus.index.treeview;

public class SimpleTreeNode
    extends AbstractTreeNode
{
    private String groupId;

    private String artifactId;

    private String version;

    public SimpleTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

}
