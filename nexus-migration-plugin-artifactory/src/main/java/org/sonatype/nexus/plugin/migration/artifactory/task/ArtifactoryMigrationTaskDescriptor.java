package org.sonatype.nexus.plugin.migration.artifactory.task;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

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

    public List<FormField> formFields()
    {
        return new ArrayList<FormField>();
    }

    @Override
    public boolean isExposed()
    {
        return false;
    }

}
