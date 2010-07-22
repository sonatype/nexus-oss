package com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractStringPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskPropertyDescriptor.class, hint = "GroovyScript", instantiationStrategy = "per-lookup" )
public class GroovyRunnerTaskPropertyDescriptor
    extends AbstractStringPropertyDescriptor
{
    public static final String ID = "groovyScript";

    public GroovyRunnerTaskPropertyDescriptor()
    {
        setHelpText( "The following properties are exposed: ant, an AntBuilder object; task, the current executing task; plexus, a PlexusContainer object; logger, a logger object." );
        setRequired( true );
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Groovy Script";
    }

    @Override
    public String getType()
    {
        return "textarea";
    }

}
