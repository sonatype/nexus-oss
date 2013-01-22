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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
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

import com.google.common.base.Function;

/**
 * Local walker strategy, that uses {@link Walker} to scan contents of local storage.
 * 
 * @author cstamas
 */
@Named( LocalWalkerStrategy.ID )
@Singleton
public class LocalWalkerStrategy
    extends AbstractStrategy<MavenHostedRepository>
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
    public EntrySource discover( final MavenHostedRepository mavenRepository )
        throws IOException
    {
        final WalkerContext context =
            new DefaultWalkerContext( mavenRepository, new ResourceStoreRequest( "/" ), new DefaultStoreWalkerFilter(),
                true );
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
        return new ArrayListEntrySource( getAllLeafPaths( prefixCollectorProcessor.getParentOMatic(),
            config.getLocalScrapeDepth() ) );
    }

    // ==

    protected List<String> getAllLeafPaths( final ParentOMatic parentOMatic, final int maxDepth )
    {
        // cut the tree
        if ( maxDepth != Integer.MAX_VALUE )
        {
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
        }
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
                    if ( thePath.endsWith( "/" ) )
                    {
                        paths.add( thePath.substring( 0, thePath.length() - 1 ) );
                    }
                    else
                    {
                        paths.add( thePath );
                    }
                }
                return null;
            }
        };
        parentOMatic.applyRecursively( parentOMatic.getRoot(), markedCollector );
        return paths;
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
        public void processItem( final WalkerContext context, final StorageItem item )
            throws Exception
        {
            if ( item instanceof StorageFileItem )
            {
                parentOMatic.addPath( item.getPath() );
            }
        }
    }

}
