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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalStrategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyResult;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;

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
        // "last resort"
        super( Integer.MAX_VALUE, ID );
        this.config = checkNotNull( config );
        this.walker = checkNotNull( walker );
    }

    @Override
    public StrategyResult discover( final MavenHostedRepository mavenRepository )
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
        final ParentOMatic parentOMatic = prefixCollectorProcessor.getParentOMatic();
        if ( parentOMatic.getRoot().isLeaf() )
        {
            // tree is basically empty, so make the list too
            return new StrategyResult( "Repository crawled successfully (is empty)", new ArrayListEntrySource(
                Collections.<String> emptyList() ) );
        }
        else
        {
            return new StrategyResult( "Repository crawled successfully", new ArrayListEntrySource( getAllLeafPaths(
                parentOMatic, config.getLocalScrapeDepth() ) ) );
        }
    }

    // ==

    protected List<String> getAllLeafPaths( final ParentOMatic parentOMatic, final int maxDepth )
    {
        // cut the tree
        if ( maxDepth != Integer.MAX_VALUE )
        {
            parentOMatic.cutNodesDeeperThan( maxDepth );
        }
        // get leafs
        return parentOMatic.getAllLeafPaths();
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
