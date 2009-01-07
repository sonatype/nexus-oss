/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.treeview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;

public abstract class AbstractTreeNode
    implements TreeNode
{
    private String type;

    private boolean leaf;

    private String nodeName;

    private String path;

    private List<TreeNode> children;

    private String groupId;

    private String artifactId;

    private String version;

    private String repositoryId;

    private transient final IndexTreeView treeView;

    private transient final TreeNodeFactory factory;

    public AbstractTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        this.treeView = tview;

        this.factory = factory;
    }

    public Type getType()
    {
        return Type.valueOf( type );
    }

    public void setType( Type type )
    {
        this.type = type.name();
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

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId( String repositoryId )
    {
        this.repositoryId = repositoryId;
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

    public TreeNode findChildByPath( String path, Type type )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        for ( TreeNode child : getChildren() )
        {
            if ( path.equals( child.getPath() ) && type.equals( child.getType() ) )
            {
                return child;
            }
        }

        return null;
    }
}
