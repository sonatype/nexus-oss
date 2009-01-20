/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.index.treeview;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.TreeNode.Type;
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

    public DefaultMergedTreeNodeFactory( IndexingContext ctx, Repository repository )
    {
        super( ctx );

        this.repository = repository;
    }

    public Repository getRepository()
    {
        return repository;
    }

    protected TreeNode decorateArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path, TreeNode node )
    {
        ResourceStoreRequest request = getResourceStoreRequest( path );

        DefaultMergedTreeNode mnode = (DefaultMergedTreeNode) node;

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

    protected TreeNode createNode( IndexTreeView tview, String path, boolean leaf, String nodeName, Type type )
    {
        TreeNode result = super.createNode( tview, path, leaf, nodeName, type );

        result.setRepositoryId( getRepository().getId() );

        return result;
    }

    protected TreeNode instantiateNode( IndexTreeView tview, String path, boolean leaf, String nodeName )
    {
        return new DefaultMergedTreeNode( tview, this );
    }

    protected ResourceStoreRequest getResourceStoreRequest( String path )
    {
        return new ResourceStoreRequest( path, true );
    }

}
