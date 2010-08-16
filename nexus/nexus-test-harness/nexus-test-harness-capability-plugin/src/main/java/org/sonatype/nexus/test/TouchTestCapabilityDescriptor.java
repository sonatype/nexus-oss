package org.sonatype.nexus.test;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;

@Component( role = CapabilityDescriptor.class, hint = TouchTestCapability.ID )
public class TouchTestCapabilityDescriptor
    implements CapabilityDescriptor
{
    public static final String FIELD_REPO_OR_GROUP_ID = "repoOrGroupId";

    public static final String FIELD_MSG_ID = "message";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField( FIELD_REPO_OR_GROUP_ID, FormField.MANDATORY );

    private final StringTextFormField msgField = new StringTextFormField( FIELD_MSG_ID, "Message", "Message help text",
                                                                          FormField.MANDATORY );

    public String id()
    {
        return TouchTestCapability.ID;
    }

    public String name()
    {
        return "Touch Test Capability";
    }

    public List<FormField> formFields()
    {
        List<FormField> fields = new ArrayList<FormField>();
        fields.add( repoField );
        fields.add( msgField );
        return fields;
    }

    public boolean isExposed()
    {
        return true;
    }

}
