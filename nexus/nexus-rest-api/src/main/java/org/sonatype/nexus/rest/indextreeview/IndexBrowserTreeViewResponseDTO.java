/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.indextreeview;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "indexBrowserTreeViewResponse" )
public class IndexBrowserTreeViewResponseDTO
{
    private IndexBrowserTreeNode data;

    public IndexBrowserTreeViewResponseDTO()
    {
    }

    public IndexBrowserTreeViewResponseDTO( IndexBrowserTreeNode node )
    {
        this.data = node;
    }

    public IndexBrowserTreeNode getData()
    {
        return data;
    }

    public void setData( IndexBrowserTreeNode data )
    {
        this.data = data;
    }
}
