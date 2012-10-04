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
package org.sonatype.nexus.plugins.shell.task;

import static org.sonatype.nexus.plugins.shell.task.ShellExecutionTaskDescriptor.COMMAND_FIELD_ID;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;

import org.sonatype.nexus.plugins.yum.execution.CommandLineExecutor;

@Component( role = SchedulerTask.class, hint = ShellExecutionTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class ShellExecutionTask
    extends AbstractNexusTask<Object>
{
    private static final String SYSTEM_ACTION = "EXECUTE_SHELL_SCRIPT";

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( isValidCommand() )
        {
            final int exitCode = new CommandLineExecutor().exec( getCommand() );
            if ( exitCode != 0 )
            {
                throw new RuntimeException( format( "Execution of command '%s' faild with exit code %d.", getCommand(),
                    exitCode ) );
            }
        }

        return null;
    }

    private boolean isValidCommand()
    {
        return ( getCommand() != null ) && isNotBlank( getCommand() );
    }

    @Override
    protected String getAction()
    {
        return SYSTEM_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Executing shell script : " + getCommand();
    }

    public String getCommand()
    {
        return getParameters().get( COMMAND_FIELD_ID );
    }

    public void setCommand( String command )
    {
        getParameters().put( COMMAND_FIELD_ID, command );
    }
}
