/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index.treeview;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.TreeNode.Type;

/**
 * A default implementation of TreeNodeFactory, that is failry simple to extend.
 * 
 * @author cstamas
 */
public class DefaultTreeNodeFactory
    implements TreeNodeFactory
{
    private IndexingContext context;

    public DefaultTreeNodeFactory( IndexingContext ctx )
    {
        this.context = ctx;
    }

    public IndexingContext getIndexingContext()
    {
        return context;
    }

    public TreeNode createGNode( IndexTreeView tview, String path, String groupName )
    {
        TreeNode result = createNode( tview, path, false, groupName, Type.G );

        return decorateGNode( tview, path, groupName, result );
    }

    protected TreeNode decorateGNode( IndexTreeView tview, String path, String groupName, TreeNode node )
    {
        return node;
    }

    public TreeNode createANode( IndexTreeView tview, ArtifactInfo ai, String path )
    {
        TreeNode result = createNode( tview, path, false, ai.artifactId, Type.A );

        result.setGroupId( ai.groupId );

        result.setArtifactId( ai.artifactId );

        return decorateANode( tview, ai, path, result );
    }

    protected TreeNode decorateANode( IndexTreeView tview, ArtifactInfo ai, String path, TreeNode node )
    {
        return node;
    }

    public TreeNode createVNode( IndexTreeView tview, ArtifactInfo ai, String path )
    {
        TreeNode result = createNode( tview, path, false, ai.version, Type.V );

        result.setGroupId( ai.groupId );

        result.setArtifactId( ai.artifactId );

        result.setVersion( ai.version );

        return decorateVNode( tview, ai, path, result );
    }

    protected TreeNode decorateVNode( IndexTreeView tview, ArtifactInfo ai, String path, TreeNode node )
    {
        return node;
    }

    public TreeNode createArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path )
    {
        StringBuffer sb = new StringBuffer( ai.artifactId ).append( "-" ).append( ai.version );

        if ( ai.classifier != null )
        {
            sb.append( "-" ).append( ai.classifier );
        }

        sb.append( "." ).append( ai.fextension == null ? "jar" : ai.fextension );

        TreeNode result = createNode( tview, path, true, sb.toString(), Type.artifact );

        result.setGroupId( ai.groupId );

        result.setArtifactId( ai.artifactId );

        result.setVersion( ai.version );

        return decorateArtifactNode( tview, ai, path, result );
    }

    protected TreeNode decorateArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path, TreeNode node )
    {
        return node;
    }

    protected TreeNode createNode( IndexTreeView tview, String path, boolean leaf, String nodeName, Type type )
    {
        TreeNode result = instantiateNode( tview, path, leaf, nodeName );

        result.setPath( path );

        result.setType( type );

        result.setLeaf( leaf );

        result.setNodeName( nodeName );

        result.setRepositoryId( getIndexingContext().getRepositoryId() );

        return result;
    }

    protected TreeNode instantiateNode( IndexTreeView tview, String path, boolean leaf, String nodeName )
    {
        return new DefaultTreeNode( tview, this );
    }

}
