package com.sonatype.nexus.plugin.groovyconsole.tasks;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.AntBuilder;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.scheduling.SchedulerTask;

import com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors.GroovyRunnerTaskDescriptor;
import com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors.properties.GroovyRunnerTaskPropertyDescriptor;

@Component( role = SchedulerTask.class, hint = GroovyRunnerTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class GroovyRunnerTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{
    @Requirement
    private PlexusContainer plexus;

    public String getGroovyScript()
    {
        return getParameters().get( GroovyRunnerTaskPropertyDescriptor.ID );
    }

    public void setGroovyScript( String groovyScript )
    {
        getParameters().put( GroovyRunnerTaskPropertyDescriptor.ID, groovyScript );
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
