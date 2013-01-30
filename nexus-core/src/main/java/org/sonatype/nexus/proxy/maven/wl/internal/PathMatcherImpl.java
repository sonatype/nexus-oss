/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.ParentOMatic.Payload;
import org.sonatype.nexus.util.Node;
import org.sonatype.nexus.util.PathUtils;

import com.google.common.annotations.VisibleForTesting;

/**
 * Default implementation of {@link PathMatcher}. Implemented using {@link ParentOMatic}, and performs matching by
 * building a maximized (capped) tree in memory out of path entries.
 * 
 * @author cstamas
 */
public class PathMatcherImpl
    implements PathMatcher
{
    private final Node<Payload> root;

    /**
     * Constructor.
     * 
     * @param entries
     */
    public PathMatcherImpl( final List<String> entries )
    {
        this( entries, Integer.MAX_VALUE );
    }

    /**
     * Constructor.
     * 
     * @param entries
     * @param maxDepth
     */
    public PathMatcherImpl( final List<String> entries, final int maxDepth )
    {
        checkArgument( maxDepth >= 2 );
        this.root = buildRoot( checkNotNull( entries ), maxDepth );
    }

    @Override
    public boolean matches( final String path )
    {
        final List<String> pathElements = PathUtils.elementsOf( path );
        Node<Payload> currentNode = root;
        for ( String pathElement : pathElements )
        {
            currentNode = currentNode.getChildByLabel( pathElement );
            if ( currentNode == null || currentNode.isLeaf() )
            {
                break;
            }
        }
        // since we add marked paths, and keepMarkedNodesOnly=true, all the marked paths will be leafs anyway.
        // also, after tree cutting, the longer nodes are also leafs (that had some marked sibling), so check for leafs
        // only, see buildRoot
        return currentNode != null && ( currentNode.isLeaf() );
    }


    @Override
    public boolean contains( final String path )
    {
        final List<String> pathElements = PathUtils.elementsOf( path );
        Node<Payload> currentNode = root;
        for ( String pathElement : pathElements )
        {
            currentNode = currentNode.getChildByLabel( pathElement );
            if ( currentNode == null || currentNode.isLeaf() )
            {
                break;
            }
        }
        // This returns leafs but also parents. If not is not null, it means "we are on right path", like in case of
        // one entry "/com/sonatype", contains("/com") would return true.
        return currentNode != null;
    }

    // ==

    @VisibleForTesting
    protected Node<Payload> getRoot()
    {
        return root;
    }

    protected Node<Payload> buildRoot( final List<String> entries, final int maxDepth )
    {
        // no rule B!
        final ParentOMatic parentOMatic = new ParentOMatic( true, true, false );
        for ( String entry : entries )
        {
            parentOMatic.addAndMarkPath( entry );
        }
        if ( maxDepth != Integer.MAX_VALUE )
        {
            // cut the tree to maxDepth
            parentOMatic.cutNodesDeeperThan( maxDepth );
        }
        return parentOMatic.getRoot();
    }
}
