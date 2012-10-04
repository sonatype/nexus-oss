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
package org.sonatype.nexus.plugins.yum.repository.task;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = "WaitTask", instantiationStrategy = "per-lookup" )
public class WaitTask
    extends AbstractNexusTask<Object>
{
    public static final Logger LOG = LoggerFactory.getLogger( WaitTask.class );

    @Override
    protected Object doRun()
        throws Exception
    {
        LOG.info( "Go to sleep for a sec." );
        Thread.sleep( 1000 );
        return null;
    }

    @Override
    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        return true;
    }

    @Override
    protected String getAction()
    {
        return "Wait";
    }

    @Override
    protected String getMessage()
    {
        return "Wait";
    }

}
