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

import org.sonatype.nexus.proxy.maven.wl.PrefixSource;

/**
 * Prefix source that merges multiple {@link PrefixSource}s into one.
 * 
 * @author cstamas
 */
public class MergingPrefixSource
    extends ArrayListPrefixSource
{
    /**
     * Constructor.
     * 
     * @param prefixSources prefix sources that you want to have merged.
     * @throws IOException
     */
    public MergingPrefixSource( final List<PrefixSource> prefixSources )
        throws IOException
    {
        super( mergeEntries( prefixSources ) );
    }

    protected static List<String> mergeEntries( final List<PrefixSource> prefixSources )
        throws IOException
    {
        final LinkedHashSet<String> result = new LinkedHashSet<String>();
        for ( final PrefixSource prefixSource : prefixSources )
        {
            result.addAll( prefixSource.readEntries() );
        }
        return new ArrayList<String>( result );
    }
}
