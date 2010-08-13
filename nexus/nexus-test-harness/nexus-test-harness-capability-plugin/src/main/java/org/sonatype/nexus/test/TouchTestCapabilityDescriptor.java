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

        FormField repoField = new RepoOrGroupComboFormField();
        repoField.setHelpText( "Select the repository or repository group to which this capability applies" );
        repoField.setId( FIELD_REPO_OR_GROUP_ID );
        repoField.setLabel( "Repository/Group" );
        repoField.setRequired( true );
        fields.add( repoField );

        FormField msgField = new StringTextFormField();
        msgField.setHelpText( "Message help text" );
        msgField.setId( FIELD_MSG_ID );
        msgField.setLabel( "Message" );
        msgField.setRequired( true );
        fields.add( msgField );

        return fields;
    }

    public boolean isExposed()
    {
        return true;
    }

}
