/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.rt.boot;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * The sole purpose of this EventInspector is to turn of "blocking commits" (equivalent of "old" behavior) on Indexer,
 * on Maven Indexer more precisely, since async nature of it borks ITs. This event inspector just puts Indexer into
 * "sync" mode (default is async).
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ITIndexerActivationEventInspector" )
public class ITIndexerActivationEventInspector
    implements EventInspector
{
    @Override
    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof PluginActivatedEvent )
        {
            PluginActivatedEvent pa = (PluginActivatedEvent) evt;

            return "nexus-indexer-lucene-plugin".equals( pa.getPluginDescriptor().getPluginCoordinates().getArtifactId() );
        }

        return false;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        // Note: in ITs we want to make Indexer perform blocking commits.
        // Since MavenIndexer 4.0, it performs async commits by default, meaning that no "helper" from Nexus
        // is able to tell and potentially block (see EventInspectorsUtil#waitForCalmPeriod() as example) execution
        // up to the moment when readers are refreshed (indexing operation IS done, but readers will not "see" the
        // change without reopening those).
        // By having this switch, we are switching Maven Indexer back into "blocking" mode as it was before 4.0.
        // The proper fix is to make all Indexer related ITs behave "properly" (with some heuristics?), and have some
        // sort of "try-wait-try-failAfterSomeRetries" the search operation itself.
        System.setProperty( "mavenIndexerBlockingCommits", Boolean.TRUE.toString() );
    }
}
