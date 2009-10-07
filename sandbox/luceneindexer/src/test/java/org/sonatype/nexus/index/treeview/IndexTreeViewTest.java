/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.treeview;

import java.io.File;

import org.sonatype.nexus.index.AbstractNexusIndexerTest;
import org.sonatype.nexus.index.NexusIndexer;

public class IndexTreeViewTest
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/repo" );

    protected IndexTreeView indexTreeView;

    protected boolean debug = false;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexTreeView = lookup( IndexTreeView.class );
    }

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "test-minimal",
            "test",
            repo,
            indexDir,
            null,
            null,
            MIN_CREATORS );
        nexusIndexer.scan( context );
    }

    protected int prettyPrint( boolean debug, TreeNode node, int level )
        throws Exception
    {
        if ( debug )
        {
            System.out.print( node.getPath() + " := " + node.getNodeName() + ", type=" + node.getType() );
            System.out.println();
        }

        int files = node.isLeaf() ? 1 : 0;

        if ( !node.isLeaf() )
        {
            for ( TreeNode child : node.listChildren() )
            {
                files += prettyPrint( debug, child, level + 2 );
            }
        }

        if ( debug && level == 0 )
        {
            System.out.println( " ===== " );
            System.out.println( " TOTAL LEAFS:  " + files );
        }

        return files;
    }

    public void testRoot()
        throws Exception
    {
        TreeNode root = indexTreeView.listNodes( new DefaultTreeNodeFactory( context ), "/" );

        int leafsFound = prettyPrint( debug, root, 0 );

        assertEquals( "The group name should be here", "/", root.getNodeName() );
        assertEquals( 12, root.getChildren().size() );
        assertEquals( 49, leafsFound );
    }

    public void testPathIsAboveRealGroup()
        throws Exception
    {
        TreeNode root = indexTreeView.listNodes( new DefaultTreeNodeFactory( context ), "/org/" );

        int leafsFound = prettyPrint( debug, root, 0 );

        assertEquals( "The group name should be here", "org", root.getNodeName() );
        assertEquals( 4, root.getChildren().size() );
        assertEquals( 22, leafsFound );
    }

    public void testPathIsRealGroup()
        throws Exception
    {
        TreeNode root = indexTreeView.listNodes( new DefaultTreeNodeFactory( context ), "/org/slf4j/" );

        int leafsFound = prettyPrint( debug, root, 0 );

        assertEquals( "The group name should be here", "slf4j", root.getNodeName() );
        assertEquals( 3, root.getChildren().size() );
        assertEquals( 10, leafsFound );
    }

    public void testPathIsRealGroupArtifact()
        throws Exception
    {
        TreeNode root = indexTreeView.listNodes( new DefaultTreeNodeFactory( context ), "/org/slf4j/slf4j-log4j12/" );

        int leafsFound = prettyPrint( debug, root, 0 );

        assertEquals( "slf4j-log4j12", root.getNodeName() );
        assertEquals( 1, root.getChildren().size() );
        assertEquals( 4, leafsFound );
    }

    public void testPathIsRealGroupArtifactVersion()
        throws Exception
    {
        TreeNode root = indexTreeView.listNodes(
            new DefaultTreeNodeFactory( context ),
            "/org/slf4j/slf4j-log4j12/1.4.1/" );

        int leafsFound = prettyPrint( debug, root, 0 );

        assertEquals( "The group name should be here", "1.4.1", root.getNodeName() );
        assertEquals( 1, root.getChildren().size() );
        assertEquals( 4, leafsFound );
    }
}
