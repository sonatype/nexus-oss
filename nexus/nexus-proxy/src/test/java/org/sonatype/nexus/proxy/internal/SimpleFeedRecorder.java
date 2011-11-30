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
package org.sonatype.nexus.proxy.internal;

import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.timeline.Entry;
import com.google.common.base.Predicate;

@Component( role = FeedRecorder.class )
public class SimpleFeedRecorder
    implements FeedRecorder

{
    @Override
    public void addErrorWarningEvent( final String action, final String message )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addErrorWarningEvent( final String action, final String message, final Throwable throwable )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addNexusArtifactEvent( final NexusArtifactEvent nae )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addSystemEvent( final String action, final String message )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addAuthcAuthzEvent( final AuthcAuthzEvent evt )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SystemProcess systemProcessStarted( final String action, final String message )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void systemProcessFinished( final SystemProcess prc, final String finishMessage )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void systemProcessCanceled( final SystemProcess prc, final String cancelMessage )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void systemProcessBroken( final SystemProcess prc, final Throwable e )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<NexusArtifactEvent> getNexusArtifectEvents( final Set<String> subtypes, final Integer from,
                                                            final Integer count,
                                                            final Predicate<Entry> filter )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SystemEvent> getSystemEvents( final Set<String> subtypes, final Integer from, final Integer count,
                                              final Predicate<Entry> filter )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<AuthcAuthzEvent> getAuthcAuthzEvents( final Set<String> subtypes, final Integer from,
                                                      final Integer count,
                                                      final Predicate<Entry> filter )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ErrorWarningEvent> getErrorWarningEvents( final Set<String> subtypes, final Integer from,
                                                          final Integer count,
                                                          final Predicate<Entry> filter )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
