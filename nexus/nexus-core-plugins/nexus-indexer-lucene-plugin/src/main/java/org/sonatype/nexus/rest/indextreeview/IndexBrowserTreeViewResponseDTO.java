/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.indextreeview;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * REST Response object received after request an item from the index browser tree,
 * contains the typical 'data' parameter, which is the tree item.
 * 
 * @version $Revision$ $Date$
 */
@XStreamAlias( "indexBrowserTreeViewResponse" )
public class IndexBrowserTreeViewResponseDTO
{
    /**
     * The tree node and its details.
     */
    private IndexBrowserTreeNode data;

    /**
     * Default constructor
     */
    public IndexBrowserTreeViewResponseDTO()
    {
    }

    /**
     * Constructor with TreeNode
     * 
     * @param IndexBrowserTreeNode
     */
    public IndexBrowserTreeViewResponseDTO( IndexBrowserTreeNode node )
    {
        this.data = node;
    }

    /**
     * Get the tree node and its details.
     * 
     * @return IndexBrowserTreeNode
     */
    public IndexBrowserTreeNode getData()
    {
        return data;
    }

    /**
     * Set the tree node and its details.
     * 
     * @param IndexBrowserTreeNode
     */
    public void setData( IndexBrowserTreeNode data )
    {
        this.data = data;
    }
}
