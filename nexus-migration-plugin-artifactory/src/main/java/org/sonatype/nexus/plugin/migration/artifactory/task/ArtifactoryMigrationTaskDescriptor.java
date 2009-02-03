package org.sonatype.nexus.plugin.migration.artifactory.task;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "ArtifactoryMigration", description = "Artifactory Migration" )
public class ArtifactoryMigrationTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    
    public static final String ID = "ArtifactoryMigrationTask";

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Artifactory Migration";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        return new ArrayList<ScheduledTaskPropertyDescriptor>();
    }

    @Override
    public boolean isExposed()
    {
        return false;
    }

    
    
}
