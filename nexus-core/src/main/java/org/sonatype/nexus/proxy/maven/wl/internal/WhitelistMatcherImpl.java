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

import javax.annotation.Nullable;

import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.ParentOMatic.Payload;
import org.sonatype.nexus.util.Node;
import org.sonatype.nexus.util.PathUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

/**
 * Default implementation of {@link WhitelistMatcher}. Implemented using {@link ParentOMatic}, and performs matching by
 * building a maximized (capped) tree in memory out of path entries.
 * 
 * @author cstamas
 */
public class WhitelistMatcherImpl
    implements WhitelistMatcher
{
    private final int maxDepth;

    private final Node<Payload> root;

    /**
     * Constructor.
     * 
     * @param entries
     * @param maxDepth
     */
    public WhitelistMatcherImpl( final List<String> entries, final int maxDepth )
    {
        checkArgument( maxDepth >= 2 );
        this.maxDepth = maxDepth;
        this.root = buildRoot( checkNotNull( entries ), maxDepth );
    }

    @Override
    public int getMaxDepth()
    {
        return maxDepth;
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
        return currentNode != null && currentNode.isLeaf();
    }

    // ==

    @VisibleForTesting
    protected Node<Payload> getRoot()
    {
        return root;
    }

    protected Node<Payload> buildRoot( final List<String> entries, final int maxDepth )
    {
        final ParentOMatic parentOMatic = new ParentOMatic();
        for ( String entry : entries )
        {
            parentOMatic.addPath( entry );
        }
        parentOMatic.applyRecursively( parentOMatic.getRoot(), new Function<Node<Payload>, Node<Payload>>()
        {
            @Override
            @Nullable
            public Node<Payload> apply( @Nullable Node<Payload> input )
            {
                if ( input.getDepth() == maxDepth )
                {
                    // simply "cut off" children if any
                    for ( Node<Payload> child : input.getChildren() )
                    {
                        input.removeChild( child );
                    }
                }
                return null;
            }
        } );
        return parentOMatic.getRoot();
    }
}
