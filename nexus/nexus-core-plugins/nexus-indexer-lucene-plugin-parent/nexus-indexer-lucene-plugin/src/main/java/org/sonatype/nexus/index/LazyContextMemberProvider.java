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
package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.index.context.ContextMemberProvider;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;

import com.google.common.base.Preconditions;

/**
 * A lazy {@link ContextMemberProvider} that uses a fixed list of members, but fetches {@link IndexingContext} on
 * demand. This is needed as without being lazy, Nexus looses it's capability to load up Nexus XML configuration that
 * has forward references in groups of groups (ie. a group references another group as member that is not yet loaded
 * up), as without being lazy, it ends up with {@link NoSuchRepositoryException}, in a moment method
 * {@link GroupRepository#getMemberRepositories()} is invoked. When needed (group member change, member indexing
 * disabled), this instance will be replaced anyway, as IndexingManager will update the contexts. All this
 * {@link ContextMemberProvider} needs to do for us is to defer "resolving" repository ID and trying to grab the
 * {@link IndexingContext} to as late as possible, but we need to created it early, as part of boot. Another approach
 * would be to "rewire" all the contexts upon Nexus boot, but that would require more changes in {@link IndexerManager}
 * itself.
 * 
 * @since 2.2
 * @author cstamas
 */
public class LazyContextMemberProvider
    implements ContextMemberProvider
{
    private final DefaultIndexerManager indexerManager;

    private final List<String> memberIds;

    private Collection<IndexingContext> contexts;

    public LazyContextMemberProvider( final DefaultIndexerManager indexerManager, final List<String> memberIds )
    {
        this.indexerManager = Preconditions.checkNotNull( indexerManager );
        this.memberIds = Preconditions.checkNotNull( memberIds );
    }

    @Override
    public synchronized Collection<IndexingContext> getMembers()
    {
        if ( contexts == null )
        {
            contexts = new ArrayList<IndexingContext>( memberIds.size() );
            for ( String member : memberIds )
            {
                try
                {
                    final IndexingContext indexingContext = indexerManager.getRepositoryIndexContext( member );
                    if ( indexingContext != null )
                    {
                        contexts.add( indexingContext );
                    }
                }
                catch ( NoSuchRepositoryException e )
                {

                }
            }
        }
        return contexts;
    }

}
