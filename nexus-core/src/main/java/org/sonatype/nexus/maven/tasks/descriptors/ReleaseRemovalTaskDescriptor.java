package org.sonatype.nexus.maven.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

/**
 * @since 2.5
 */
@Named
@Singleton
public class ReleaseRemovalTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{

    public static final String ID = "ReleaseRemoverTask";

    public static final String REPOSITORY_FIELD_ID = "repositoryId";

    public static final String NUMBER_OF_VERSIONS_TO_KEEP_FIELD_ID = "numberOfVersionsToKeep";

    public static final String REPOSITORY_TARGET_FIELD_ID = "repositoryTarget";

    private final List<FormField> formFields = ImmutableList.<FormField>of(
        //TODO - KR add label and helpText?
        new RepoComboFormField( REPOSITORY_FIELD_ID, FormField.MANDATORY ),
        new NumberTextFormField(
            NUMBER_OF_VERSIONS_TO_KEEP_FIELD_ID, "Number to keep", "The number of versions for each GA to keep",
            FormField.MANDATORY ),
        new StringTextFormField( REPOSITORY_TARGET_FIELD_ID, "Repository Target", "TODO - KR", FormField.OPTIONAL )
    );

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Remove Releases From Repository";
    }

    @Override
    public List<FormField> formFields()
    {
        return formFields;
    }
}
