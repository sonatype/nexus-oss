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
import static org.sonatype.nexus.util.PathUtils.elementsOf;
import static org.sonatype.nexus.util.PathUtils.pathFrom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.maven.wl.WritableEntrySource;
import org.sonatype.nexus.proxy.maven.wl.WritableEntrySourceModifier;

/**
 * Default implementation of {@link WritableEntrySourceModifier} that collects changes in memory, and flushes it at once
 * when {@link #apply()} method is invoked.
 * 
 * @author cstamas
 * @since 2.4
 */
public class WritableEntrySourceModifierImpl
    implements WritableEntrySourceModifier
{
    private final WritableEntrySource writableEntrySource;

    private final int wlMaxDepth;

    private final List<String> toBeAdded;

    private final List<String> toBeRemoved;

    private WhitelistMatcher whitelistMatcher;

    /**
     * Constructor.
     * 
     * @param writableEntrySource
     * @param wlMaxDepth
     * @throws IOException
     */
    public WritableEntrySourceModifierImpl( final WritableEntrySource writableEntrySource, final int wlMaxDepth )
        throws IOException
    {
        this.writableEntrySource = checkNotNull( writableEntrySource );
        this.wlMaxDepth = wlMaxDepth;
        this.toBeAdded = new ArrayList<String>();
        this.toBeRemoved = new ArrayList<String>();
        reset( writableEntrySource.readEntries() );
    }

    @Override
    public boolean offerEntries( final String... entries )
    {
        boolean modified = false;
        for ( String entry : entries )
        {
            final String maxedEntry = pathFrom( elementsOf( entry ), whitelistMatcher.getMaxDepth() );
            if ( !whitelistMatcher.matches( maxedEntry ) && !toBeAdded.contains( maxedEntry ) )
            {
                toBeAdded.add( maxedEntry );
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean revokeEntries( final String... entries )
    {
        boolean modified = false;
        for ( String entry : entries )
        {
            final String maxedEntry = pathFrom( elementsOf( entry ), whitelistMatcher.getMaxDepth() );
            if ( whitelistMatcher.matches( maxedEntry ) && !toBeRemoved.contains( maxedEntry ) )
            {
                toBeRemoved.add( maxedEntry );
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean hasChanges()
    {
        return !toBeRemoved.isEmpty() || !toBeAdded.isEmpty();
    }

    @Override
    public boolean apply()
        throws IOException
    {
        if ( hasChanges() )
        {
            final ArrayList<String> entries = new ArrayList<String>( writableEntrySource.readEntries() );
            entries.removeAll( toBeRemoved );
            entries.addAll( toBeAdded );
            final ArrayListEntrySource newEntries = new ArrayListEntrySource( entries );
            writableEntrySource.writeEntries( newEntries );
            reset( newEntries.readEntries() );
            return true;
        }
        return false;
    }

    @Override
    public boolean reset()
        throws IOException
    {
        if ( hasChanges() )
        {
            reset( writableEntrySource.readEntries() );
            return true;
        }
        return false;
    }

    // ==

    protected void reset( final List<String> entries )
    {
        this.toBeAdded.clear();
        this.toBeRemoved.clear();
        this.whitelistMatcher = new WhitelistMatcherImpl( entries, wlMaxDepth );
    }
}
