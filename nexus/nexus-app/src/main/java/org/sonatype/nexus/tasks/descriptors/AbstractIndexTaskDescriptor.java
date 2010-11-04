package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;

public abstract class AbstractIndexTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{

    public static final String REPO_OR_GROUP_FIELD_ID = "repositoryOrGroupId";

    public static final String RESOURCE_STORE_PATH_FIELD_ID = "resourceStorePath";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField( REPO_OR_GROUP_FIELD_ID,
        FormField.MANDATORY );

    private final StringTextFormField resourceStorePathField = new StringTextFormField( RESOURCE_STORE_PATH_FIELD_ID,
        "Repository path",
        "Enter a repository path to run the task in recursively (ie. \"/\" for root or \"/org/apache\")",
        FormField.OPTIONAL );

    private String id;

    private String name;

    public AbstractIndexTaskDescriptor( String id, String name )
    {
        super();

        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name + " Repositories Index";
    }

    @Override
    public List<FormField> formFields()
    {
        List<FormField> fields = new ArrayList<FormField>();

        fields.add( repoField );
        fields.add( resourceStorePathField );

        return fields;
    }

}