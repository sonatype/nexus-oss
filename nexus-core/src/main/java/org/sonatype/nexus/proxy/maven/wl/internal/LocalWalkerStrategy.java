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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalStrategy;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.ParentOMatic.Payload;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.util.Node;
import org.sonatype.nexus.util.PathUtils;

import com.google.common.base.Function;

/**
 * Local walker strategy, that uses {@link Walker} to scan contents of local storage.
 * 
 * @author cstamas
 */
@Named( LocalWalkerStrategy.ID )
@Singleton
public class LocalWalkerStrategy
    extends AbstractStrategy
    implements LocalStrategy
{
    protected static final String ID = "walker";

    private final WLConfig config;

    private final Walker walker;

    @Inject
    public LocalWalkerStrategy( final WLConfig config, final Walker walker )
    {
        super( ID, Integer.MAX_VALUE );
        this.config = checkNotNull( config );
        this.walker = checkNotNull( walker );
    }

    @Override
    public EntrySource discover( final MavenRepository mavenRepository )
        throws IOException
    {
        final WalkerContext context =
            new DefaultWalkerContext( mavenRepository, new ResourceStoreRequest( "/" ), new DepthFilter(
                config.getLocalScrapeDepth() ), true );
        final PrefixCollectorProcessor prefixCollectorProcessor = new PrefixCollectorProcessor();
        context.getProcessors().add( prefixCollectorProcessor );

        try
        {
            walker.walk( context );
        }
        catch ( WalkerException e )
        {
            if ( !( e.getWalkerContext().getStopCause() instanceof ItemNotFoundException ) )
            {
                // everything that is not ItemNotFound should be reported,
                // otherwise just neglect it
                throw e;
            }
        }
        return new ArrayListEntrySource( getAllLeafPaths( prefixCollectorProcessor.getParentOMatic() ) );
    }

    // ==

    protected List<String> getAllLeafPaths( final ParentOMatic parentOMatic )
    {
        // doing scanning
        final ArrayList<String> paths = new ArrayList<String>();
        final Function<Node<Payload>, Node<Payload>> markedCollector = new Function<Node<Payload>, Node<Payload>>()
        {
            @Override
            public Node<Payload> apply( Node<Payload> input )
            {
                if ( input.isLeaf() )
                {
                    final String thePath = input.getPath();
                    paths.add( thePath.substring( 0, thePath.length() - 1 ) );
                }
                return null;
            }
        };
        parentOMatic.applyRecursively( parentOMatic.getRoot(), markedCollector );
        return paths;
    }

    public static class DepthFilter
        extends DefaultStoreWalkerFilter
    {
        private final int maxDepth;

        public DepthFilter( final int maxDepth )
        {
            checkArgument( maxDepth >= 1 );
            this.maxDepth = maxDepth;
        }

        @Override
        public boolean shouldProcess( WalkerContext context, StorageItem item )
        {
            return super.shouldProcess( context, item ) && aboveOrAtMaxDepth( item.getPath() );
        }

        @Override
        public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
        {
            return super.shouldProcessRecursively( context, coll ) && aboveOrAtMaxDepth( coll.getPath() );
        }

        // ==

        protected boolean aboveOrAtMaxDepth( final String path )
        {
            return PathUtils.depthOf( path ) <= maxDepth;
        }
    }

    public static class PrefixCollectorProcessor
        extends AbstractWalkerProcessor
    {
        private final ParentOMatic parentOMatic;

        public PrefixCollectorProcessor()
        {
            this.parentOMatic = new ParentOMatic();
        }

        public ParentOMatic getParentOMatic()
        {
            return parentOMatic;
        }

        @Override
        public void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
            throws Exception
        {
            parentOMatic.addPath( coll.getPath() );
        }

        @Override
        public void processItem( final WalkerContext context, final StorageItem item )
            throws Exception
        {
            if ( item instanceof StorageFileItem )
            {
                parentOMatic.addAndMarkPath( item.getPath() );
            }
            else
            {
                parentOMatic.addPath( item.getPath() );
            }
        }
    }

}
