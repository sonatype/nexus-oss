package com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

import com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors.properties.TextAreaFormField;

@Component( role = ScheduledTaskDescriptor.class, hint = "GroovyRunner", description = "Groovy Runner Task" )
public class GroovyRunnerTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "GroovyRunnerTask";

    public static final String REPO_OR_GROUP_FIELD_ID = "repositoryOrGroupId";

    public static final String SCRIPT_FIELD_ID = "groovyScript";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField( REPO_OR_GROUP_FIELD_ID );

    private final TextAreaFormField scriptField = new TextAreaFormField( SCRIPT_FIELD_ID );

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Groovy Runner Task";
    }

    public List<FormField> formFields()
    {
        List<FormField> fields = new ArrayList<FormField>();

        fields.add( repoField );
        fields.add( scriptField );

        return fields;
    }
}
