/**
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
package org.sonatype.nexus.proxy.walker;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.util.Node;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * A helper class to "optimize" when some sort of "gathering for later processing" (most notable collection of
 * directories to start Walks from) of repository paths happens, that are to be processed in some subsequent step by
 * "walking" them (recurse them, a la "visitor" pattern). This utility class simply maintains a "tree" with
 * <em>marked</em> nodes, while the meaning of "marked" is left to class user. One assumption does exists against
 * "marked": some recursive processing is planned against it. It currently applies these simple rules to it.
 * <p>
 * <ul>
 * <li>rule A: if parent node of the currently added node is already "marked", do not mark the currently added node
 * (reason: it will be anyway processes when marked parent processing starts to recurse)</li>
 * <li>rule B: if all the children nodes of the currently added node's parent as "marked", mark the parent and unmark
 * it's children (reason: it's usually cheaper to fire off one walk level above, from parent, instead firing, for
 * example 100, independent walks from children one by one)</li>
 * </ul>
 * <p>
 * Note: all the input paths are expected to be "normalized ones": being absolute, using generic "/" character as path
 * separator (since these are NOT File paths, but just hierarchical paths of strings). For example:
 * {@link RepositoryItemUid#getPath()} returns paths like these.
 * <p>
 * This class also "optimizes" the tree size to lessen memory use. This "optimization" can be turned off, see
 * constructors.
 * <p>
 * This class makes use of {@link Node} to implement the tree hierarchy.
 * 
 * @author cstamas
 * @since 2.0
 */
public class ParentOMatic
{
    public static class Payload
    {
        private final String path;

        private boolean marked;

        public Payload( String path )
        {
            this.path = Preconditions.checkNotNull( path );
            this.marked = false;
        }

        public String getPath()
        {
            return path;
        }

        public boolean isMarked()
        {
            return marked;
        }

        public void setMarked( boolean marked )
        {
            this.marked = marked;
        }
    }

    /**
     * If true, all the nodes below marked ones will be simply cut away, to lessen tree size and hence, memory
     * consumption. They have no need to stay in memory, since the result will not need them anyway, will not be
     * returned by {@link #getMarkedPaths()}.
     */
    private final boolean optimizeTreeSize;

    /**
     * The root node.
     */
    private final Node<Payload> ROOT;

    /**
     * Creates new instance of ParentOMatic with default settings.
     */
    public ParentOMatic()
    {
        this( true );
    }

    public ParentOMatic( final boolean optimizeTreeSize )
    {
        super();
        this.optimizeTreeSize = optimizeTreeSize;
        this.ROOT = new Node<Payload>( null, "ROOT", new Payload( "/" ) );
    }

    /**
     * Adds a path to this ParentOMatic instance without marking it.
     * 
     * @param path
     * @return
     */
    public Node<Payload> addPath( final String path )
    {
        return addPath( path, true );
    }

    /**
     * Adds a path to this ParentOMatic and marks it. This might result in changes in tree that actually tries to
     * "optimize" the markings, and it may result in tree where the currently added and marked path is not marked, but
     * it's some parent is.
     * 
     * @param path
     */
    public void addAndMarkPath( final String path )
    {
        final Node<Payload> currentNode = addPath( path, false );

        // unmark children if any
        applyRecursively( currentNode, new Function<Node<Payload>, Node<Payload>>()
        {
            @Override
            public Node<Payload> apply( Node<Payload> input )
            {
                input.getPayload().setMarked( false );
                return input;
            }
        } );

        currentNode.getPayload().setMarked( true );

        // reorganize if needed
        final Node<Payload> flippedNode = reorganizeForRecursion( currentNode );

        // optimize tree size if asked for
        if ( optimizeTreeSize )
        {
            optimizeTreeSize( flippedNode );
        }
    }

    /**
     * Returns the list of the marked paths.
     * 
     * @return
     */
    public List<String> getMarkedPaths()
    {
        // doing scanning
        final ArrayList<String> markedPaths = new ArrayList<String>();
        final Function<Node<Payload>, Node<Payload>> markedCollector = new Function<Node<Payload>, Node<Payload>>()
        {
            @Override
            public Node<Payload> apply( Node<Payload> input )
            {
                if ( input.getPayload().isMarked() )
                {
                    markedPaths.add( input.getPayload().getPath() );
                }
                return null;
            }
        };
        applyRecursively( ROOT, markedCollector );
        return markedPaths;
    }

    // ==

    /**
     * "Dumps" the tree, for tests.
     * 
     * @return
     */
    public String dump()
    {
        final StringBuilder sb = new StringBuilder();
        dump( ROOT, 0, sb );
        return sb.toString();
    }

    protected void dump( final Node<Payload> node, final int depth, final StringBuilder sb )
    {
        sb.append( Strings.repeat( "  ", depth ) );
        sb.append( node.getLabel() );
        sb.append( " (" ).append( node.getPayload().getPath() ).append( ")" );
        if ( node.getPayload().isMarked() )
        {
            sb.append( "*" );
        }
        sb.append( "\n" );
        for ( Node<Payload> child : node.getChildren() )
        {
            dump( child, depth + 1, sb );
        }
    }

    // ==

    protected Node<Payload> addPath( final String path, final boolean optimize )
    {
        final List<String> pathElems = getPathElements( Preconditions.checkNotNull( path ) );
        final List<String> actualPathElems = Lists.newArrayList();

        Node<Payload> currentNode = ROOT;

        for ( String pathElem : pathElems )
        {
            actualPathElems.add( pathElem );
            final Node<Payload> node = currentNode.getChildByLabel( pathElem );

            if ( node == null )
            {
                currentNode = currentNode.addChild( pathElem, new Payload( getPathElementsAsPath( actualPathElems ) ) );
            }
            else
            {
                currentNode = node;
            }
        }

        if ( optimize )
        {
            optimizeTreeSize( currentNode );
        }

        return currentNode;
    }

    /**
     * Reorganizes the tree by applying the rules to the tree from the changed node and returns a node that was top most
     * of the flipped ones.
     * 
     * @param changedNode
     */
    protected Node<Payload> reorganizeForRecursion( final Node<Payload> changedNode )
    {
        // rule a: if parent is marked already, do not mark the child
        if ( isParentMarked( changedNode ) )
        {
            changedNode.getPayload().setMarked( false );
            return changedNode.getParent();
        }

        // rule b: if this parent's all children are marked, mark parent, unmark children
        if ( isParentAllChildMarkedForRuleB( changedNode ) )
        {
            changedNode.getParent().getPayload().setMarked( true );
            for ( Node<Payload> child : changedNode.getParent().getChildren() )
            {
                child.getPayload().setMarked( false );
            }
            return changedNode.getParent();
        }

        return changedNode;
    }

    /**
     * Optimizes the tree by making the marked nodes as leafs, basically cutting all the branches that are below marked
     * node.
     * 
     * @param changedNode
     */
    protected void optimizeTreeSize( final Node<Payload> changedNode )
    {
        // simply "cut off" children if any
        for ( Node<Payload> child : changedNode.getChildren() )
        {
            changedNode.removeChild( child );
        }
    }

    /**
     * Applies function recursively from the given node.
     * 
     * @param fromNode
     * @param modifier
     */
    protected void applyRecursively( final Node<Payload> fromNode, final Function<Node<Payload>, Node<Payload>> modifier )
    {
        modifier.apply( fromNode );

        for ( Node<Payload> child : fromNode.getChildren() )
        {
            applyRecursively( child, modifier );
        }
    }

    /**
     * Returns true if parent exists (passed in node is not ROOT), and if parent {@link Payload#isMarked()} returns
     * {@code true}.
     * 
     * @param node
     * @return
     */
    protected boolean isParentMarked( final Node<Payload> node )
    {
        final Node<Payload> parent = node.getParent();

        if ( parent != null )
        {
            if ( parent.getPayload().isMarked() )
            {
                return true;
            }
            else
            {
                return isParentMarked( parent );
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns true if parent exists (passed in node is not ROOT), and parent's all children are marked (their
     * {@link Payload#isMarked()} is {@code true} for all of them.
     * 
     * @param node
     * @return
     */
    protected boolean isParentAllChildMarkedForRuleB( final Node<Payload> node )
    {
        final Node<Payload> parent = node.getParent();

        if ( parent != null )
        {
            final List<Node<Payload>> children = parent.getChildren();

            if ( children.size() < 2 )
            {
                return false;
            }

            for ( Node<Payload> child : children )
            {
                if ( !child.getPayload().isMarked() )
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Builds a path from path elements list.
     * 
     * @param pathElems
     * @return
     */
    protected String getPathElementsAsPath( final List<String> pathElems )
    {
        final StringBuilder sb = new StringBuilder( "/" );
        for ( String elem : pathElems )
        {
            sb.append( elem ).append( "/" );
        }
        return sb.toString();
    }

    /**
     * Builds a path elements list from passed in path.
     * 
     * @param path
     * @return
     */
    protected List<String> getPathElements( final String path )
    {
        final List<String> result = Lists.newArrayList();
        final String[] elems = path.split( "/" );

        for ( String elem : elems )
        {
            if ( !Strings.isNullOrEmpty( elem ) )
            {
                result.add( elem );
            }
        }

        return result;
    }
}
