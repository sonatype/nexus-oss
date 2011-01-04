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
package org.sonatype.nexus.index.treeview;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.treeview.DefaultTreeNodeFactory;
import org.apache.maven.index.treeview.IndexTreeView;
import org.apache.maven.index.treeview.TreeNode;
import org.apache.maven.index.treeview.TreeNode.Type;
import org.apache.maven.index.treeview.TreeViewRequest;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A default implementation of merged TreeNodeFactory, that is failry simple to extend. Note: this implementation
 * assumes that index is <b>superset</b> of the local storage content, which is true for both proxied (if remote idx
 * exists and is downloaded) and hosted reposes (where actually the sets are equal)!
 * 
 * @author cstamas
 */
public class DefaultMergedTreeNodeFactory
    extends DefaultTreeNodeFactory
{
    private Repository repository;

    public DefaultMergedTreeNodeFactory( Repository repository )
    {
        super( repository.getId() );

        this.repository = repository;
    }

    public Repository getRepository()
    {
        return repository;
    }

    @Override
    protected TreeNode decorateGNode( IndexTreeView tview, TreeViewRequest req, String path, String groupName,
                                      TreeNode node )
    {
        DefaultMergedTreeNode mnode = (DefaultMergedTreeNode) super.decorateGNode( tview, req, path, groupName, node );
        mnode.setLocallyAvailable( isPathAvailable( path ) );

        return mnode;
    }

    @Override
    protected TreeNode decorateANode( IndexTreeView tview, TreeViewRequest req, ArtifactInfo ai, String path,
                                      TreeNode node )
    {
        DefaultMergedTreeNode mnode = (DefaultMergedTreeNode) super.decorateANode( tview, req, ai, path, node );
        mnode.setLocallyAvailable( isPathAvailable( path ) );

        return mnode;
    }

    @Override
    protected TreeNode decorateVNode( IndexTreeView tview, TreeViewRequest req, ArtifactInfo ai, String path,
                                      TreeNode node )
    {
        DefaultMergedTreeNode mnode = (DefaultMergedTreeNode) super.decorateVNode( tview, req, ai, path, node );
        mnode.setLocallyAvailable( isPathAvailable( path ) );

        return mnode;
    }

    @Override
    protected TreeNode decorateArtifactNode( IndexTreeView tview, TreeViewRequest req, ArtifactInfo ai, String path,
                                             TreeNode node )
    {
        DefaultMergedTreeNode mnode = (DefaultMergedTreeNode) super.decorateArtifactNode( tview, req, ai, path, node );

        ResourceStoreRequest request = getResourceStoreRequest( path );

        // default it to not available
        mnode.setLocallyAvailable( false );

        try
        {
            StorageItem item = getRepository().retrieveItem( request );

            if ( item instanceof StorageFileItem )
            {
                mnode.setLocallyAvailable( true );

                mnode.setArtifactTimestamp( item.getModified() );

                mnode.setArtifactMd5Checksum( item.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );

                mnode.setArtifactSha1Checksum( item.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );

                mnode.setInitiatorUserId( item.getAttributes().get( AccessManager.REQUEST_USER ) );

                mnode.setInitiatorIpAddress( item.getAttributes().get( AccessManager.REQUEST_REMOTE_ADDRESS ) );

                mnode.setArtifactOriginUrl( item.getRemoteUrl() );

                if ( !StringUtils.isEmpty( mnode.getArtifactOriginUrl() ) )
                {
                    mnode.setArtifactOriginReason( "cached" );
                }
                else
                {
                    mnode.setArtifactOriginReason( "deployed" );
                }
            }
        }
        catch ( ItemNotFoundException e )
        {
            // mute it, probaly not available locally
        }
        catch ( Exception e )
        {
            // TODO: do something here?
        }

        return node;
    }

    @Override
    protected TreeNode createNode( IndexTreeView tview, TreeViewRequest req, String path, boolean leaf,
                                   String nodeName, Type type )
    {
        TreeNode result = super.createNode( tview, req, path, leaf, nodeName, type );

        result.setRepositoryId( getRepository().getId() );

        return result;
    }

    @Override
    protected TreeNode instantiateNode( IndexTreeView tview, TreeViewRequest req, String path, boolean leaf,
                                        String nodeName )
    {
        return new DefaultMergedTreeNode( tview, req );
    }

    protected ResourceStoreRequest getResourceStoreRequest( String path )
    {
        return new ResourceStoreRequest( path, true );
    }

    protected boolean isPathAvailable( String path )
    {
        ResourceStoreRequest request = getResourceStoreRequest( path );

        try
        {
            return getRepository().getLocalStorage().containsItem( getRepository(), request );
        }
        catch ( Exception e )
        {
            // for whatever reason, couldn't see item, so it's not cached locally we shall say
        }

        return false;
    }

}
