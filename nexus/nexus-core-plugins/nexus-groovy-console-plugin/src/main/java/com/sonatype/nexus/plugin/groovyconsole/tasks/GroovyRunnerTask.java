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
package com.sonatype.nexus.plugin.groovyconsole.tasks;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.AntBuilder;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

import com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors.GroovyRunnerTaskDescriptor;

@Component( role = SchedulerTask.class, hint = GroovyRunnerTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class GroovyRunnerTask
    extends AbstractNexusRepositoriesTask<Object>
{
    @Requirement
    private PlexusContainer plexus;

    @Override
    protected String getRepositoryFieldId()
    {
        return GroovyRunnerTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    public String getGroovyScript()
    {
        return getParameters().get( GroovyRunnerTaskDescriptor.SCRIPT_FIELD_ID );
    }

    public void setGroovyScript( String groovyScript )
    {
        getParameters().put( GroovyRunnerTaskDescriptor.SCRIPT_FIELD_ID, groovyScript );
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        Binding binding = new Binding();
        binding.setVariable( "ant", new AntBuilder() );
        binding.setVariable( "task", this );
        binding.setVariable( "plexus", plexus );
        binding.setVariable( "logger", getLogger() );

        CompilerConfiguration config = new CompilerConfiguration( CompilerConfiguration.DEFAULT );
        GroovyShell shell = new GroovyShell( binding, config );
        String groovyScript = getGroovyScript();
        Object result = shell.evaluate( groovyScript );
        getLogger().info( "Script return: " + result );
        return result;
    }

    @Override
    protected String getAction()
    {
        return "Groovy Runner Task";
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Running Groovy Task on " + getRepositoryGroupName();
        }
        else if ( getRepositoryId() != null )
        {
            return "Running Groovy Task on " + getRepositoryName();
        }
        else
        {
            return "Running Groovy Task on all registered repositories";
        }
    }

}
