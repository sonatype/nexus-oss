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
package org.sonatype.nexus.repository.yum.internal.task;

import static org.apache.commons.io.FileUtils.listFiles;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.TaskState;

/**
 * This job scans a {@link Repository} for RPMs and adds each version to Yam.
 *
 * @author sherold
 */
@Component( role = SchedulerTask.class, hint = RepositoryScanningTask.ID, instantiationStrategy = "per-lookup" )
public class RepositoryScanningTask
    extends AbstractNexusTask<Object>
{

    private static final int MAXIMAL_PARALLEL_RUNS = 3;

    public static final String ID = "RepositoryScanningTask";

    private static final String[] RPM_EXTENSIONS = new String[]{ "rpm" };

    private Yum yum;

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( yum == null )
        {
            throw new IllegalArgumentException( "Please provide a Yum" );
        }

        scanRepository();

        return null;
    }

    private void scanRepository()
    {
        getLogger().debug(
            "Scanning repository '{}' base dir '{}' for RPMs ", yum.getRepository().getId(), yum.getBaseDir()
        );

        // TODO this should use Nexus walker
        for ( File file : listFiles( yum.getBaseDir(), RPM_EXTENSIONS, true ) )
        {
            yum.addVersion( file.getParentFile().getName() );
        }
    }

    @Override
    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        if ( activeTasks.containsKey( ID ) )
        {
            int activeRunningTasks = 0;
            for ( ScheduledTask<?> task : activeTasks.get( ID ) )
            {
                if ( TaskState.RUNNING.equals( task.getTaskState() ) )
                {
                    activeRunningTasks++;
                }
            }
            return activeRunningTasks < MAXIMAL_PARALLEL_RUNS;
        }
        else
        {
            return true;
        }
    }

    @Override
    protected String getAction()
    {
        return "scanning";
    }

    @Override
    protected String getMessage()
    {
        return "Scanning repository '" + yum.getRepository().getId() + "'";
    }

    public void setYum( Yum yum )
    {
        this.yum = yum;
    }

}
