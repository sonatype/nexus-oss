/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.timeline;

import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import com.google.common.base.Predicate;

/**
 * A "smart" timeline that is able to detect presence of real timeline implementation. It looks for a
 * {@code NexusTimeline} role and hint of {@code "real"}. If component like this is found, it is used, if not NOOP
 * timeline is used.
 *
 * @author: cstamas
 * @since 1.10.0
 */
@Component( role = NexusTimeline.class )
public class RedirectingTimeline
    extends AbstractLoggingComponent
    implements NexusTimeline
{

    @Requirement
    private PlexusContainer plexusContainer;

    private volatile NexusTimeline nexusTimeline;

    public RedirectingTimeline()
    {
        this.nexusTimeline = NoopTimeline.INSTANCE;
    }

    public synchronized void tryToActivateTimeline()
    {
        try
        {
            this.nexusTimeline = plexusContainer.lookup( NexusTimeline.class, "real" );

            getLogger().info( "Timeline present and enabled." );
        }
        catch ( Exception e )
        {
            getLogger().info( "Tried to enable Timeline but failed, fallback to NOOP Timeline." );

            // silent
            this.nexusTimeline = NoopTimeline.INSTANCE;
        }
    }

    protected NexusTimeline getDelegate()
    {
        return nexusTimeline;
    }

    @Override
    public void add( final long timestamp, final String type, final String subType, final Map<String, String> data )
    {
        getDelegate().add( timestamp, type, subType, data );
    }

    @Override
    public Entries retrieve( final int fromItem, final int count, final Set<String> types, final Set<String> subtypes,
                             final Predicate<Entry> filter )
    {
        return getDelegate().retrieve( fromItem, count, types, subtypes, filter );
    }

    @Override
    public int purgeOlderThan( final long timestamp, final Set<String> types, final Set<String> subTypes,
                               final Predicate<Entry> filter )
    {
        return getDelegate().purgeOlderThan( timestamp, types, subTypes, filter );
    }
}
