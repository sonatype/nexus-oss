/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.indextreeview;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.treeview.IndexTreeView;
import org.apache.maven.index.treeview.TreeNode;
import org.apache.maven.index.treeview.TreeViewRequest;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.treeview.DefaultMergedTreeNodeFactory;
import org.sonatype.nexus.proxy.repository.Repository;

public class IndexBrowserTreeNodeFactory
    extends DefaultMergedTreeNodeFactory
{
    private String baseLinkUrl;

    public IndexBrowserTreeNodeFactory( Repository repository, String baseLinkUrl )
    {
        super( repository );
        this.baseLinkUrl = baseLinkUrl;
    }

    @Override
    protected TreeNode decorateArtifactNode( IndexTreeView tview, TreeViewRequest req, ArtifactInfo ai, String path,
                                             TreeNode node )
    {
        IndexBrowserTreeNode iNode = (IndexBrowserTreeNode) super.decorateArtifactNode( tview, req, ai, path, node );

        iNode.setClassifier( ai.classifier );
        iNode.setExtension( ai.fextension );
        iNode.setPackaging( ai.packaging );
        iNode.setArtifactUri( buildArtifactUri( iNode ) );
        iNode.setPomUri( buildPomUri( iNode ) );

        return iNode;
    }

    @Override
    protected TreeNode instantiateNode( IndexTreeView tview, TreeViewRequest req, String path, boolean leaf,
                                        String nodeName )
    {
        return new IndexBrowserTreeNode( tview, req );
    }

    protected String buildArtifactUri( IndexBrowserTreeNode node )
    {
        if ( StringUtils.isEmpty( node.getPackaging() ) || "pom".equals( node.getPackaging() ) )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append( "?r=" );
        sb.append( node.getRepositoryId() );
        sb.append( "&g=" );
        sb.append( node.getGroupId() );
        sb.append( "&a=" );
        sb.append( node.getArtifactId() );
        sb.append( "&v=" );
        sb.append( node.getVersion() );
        sb.append( "&p=" );
        sb.append( node.getPackaging() );

        return this.baseLinkUrl + sb.toString();
    }

    protected String buildPomUri( IndexBrowserTreeNode node )
    {
        if ( StringUtils.isNotEmpty( node.getClassifier() ) )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append( "?r=" );
        sb.append( node.getRepositoryId() );
        sb.append( "&g=" );
        sb.append( node.getGroupId() );
        sb.append( "&a=" );
        sb.append( node.getArtifactId() );
        sb.append( "&v=" );
        sb.append( node.getVersion() );
        sb.append( "&p=pom" );

        return this.baseLinkUrl + sb.toString();
    }
}
