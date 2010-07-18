package com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "GroovyRunner", description = "Groovy Runner Task" )
public class GroovyRunnerTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "GroovyRunnerTask";

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "RepositoryOrGroup" )
    private ScheduledTaskPropertyDescriptor repositoryOrGroupId;

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "GroovyScript" )
    private ScheduledTaskPropertyDescriptor groovyScript;

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Groovy Runner Task";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();
        properties.add( repositoryOrGroupId );
        properties.add( groovyScript );
        return properties;
    }
}
