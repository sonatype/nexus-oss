package org.sonatype.nexus.index.treeview;

import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;

public interface TreeNode
{
    public enum Type
    {
        G, A, V, artifact
    };

    Type getType();

    void setType( Type t );

    boolean isLeaf();

    void setLeaf( boolean leaf );

    String getNodeName();

    void setNodeName( String name );

    String getPath();

    void setPath( String path );

    String getGroupId();

    void setGroupId( String groupId );

    String getArtifactId();

    void setArtifactId( String artifactId );

    String getVersion();

    void setVersion( String version );

    String getRepositoryId();

    void setRepositoryId( String repositoryId );

    List<TreeNode> getChildren();

    List<TreeNode> listChildren()
        throws IndexContextInInconsistentStateException,
            IOException;

    TreeNode findChildByPath( String path, Type type )
        throws IndexContextInInconsistentStateException,
            IOException;
}
