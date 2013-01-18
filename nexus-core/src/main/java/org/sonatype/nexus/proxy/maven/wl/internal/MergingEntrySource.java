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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.sonatype.nexus.proxy.maven.wl.EntrySource;

import com.google.common.base.Preconditions;

/**
 * Entry source that merges multiple {@link EntrySource}s into one. It retains order by watch for uniqueness.
 * 
 * @author cstamas
 */
public class MergingEntrySource
    implements EntrySource
{
    private final List<EntrySource> entrySources;

    /**
     * Constructor.
     * 
     * @param entrySources entry sources that you want to have merged.
     */
    public MergingEntrySource( final List<EntrySource> entrySources )
    {
        this.entrySources = Preconditions.checkNotNull( entrySources );
    }

    @Override
    public boolean exists()
    {
        return true;
    }

    @Override
    public List<String> readEntries()
        throws IOException
    {
        final LinkedHashSet<String> set = new LinkedHashSet<String>();
        for ( EntrySource entrySource : entrySources )
        {
            set.addAll( entrySource.readEntries() );
        }
        return new ArrayList<String>( set );
    }

    @Override
    public long getLostModifiedTimestamp()
    {
        long result = -1;
        for ( EntrySource entrySource : entrySources )
        {
            result = Math.max( result, entrySource.getLostModifiedTimestamp() );
        }
        return result;
    }
}
