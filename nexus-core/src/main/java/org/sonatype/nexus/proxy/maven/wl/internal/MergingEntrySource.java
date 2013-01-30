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
import java.util.List;

import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.walker.ParentOMatic;

/**
 * Entry source that merges multiple {@link EntrySource}s into one. It retains "correctness" of the result, by watching
 * "least specific" to win (and most specific removed) in result list.
 * 
 * @author cstamas
 */
public class MergingEntrySource
    extends ArrayListEntrySource
{
    /**
     * Constructor.
     * 
     * @param entrySources entry sources that you want to have merged.
     * @throws IOException
     */
    public MergingEntrySource( final List<EntrySource> entrySources )
        throws IOException
    {
        super( mergeEntries( entrySources ) );
    }

    protected static List<String> mergeEntries( final List<EntrySource> entrySources )
        throws IOException
    {
        // no rule B!
        final ParentOMatic parentOMatic = new ParentOMatic( true, true, false );
        for ( final EntrySource entrySource : entrySources )
        {
            for ( final String entry : entrySource.readEntries() )
            {
                parentOMatic.addAndMarkPath( entry );
            }
        }
        return parentOMatic.getAllLeafPaths();
    }
}
