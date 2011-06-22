package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;

@Singleton
@Named( P2MetadataGeneratorCapability.ID )
public class P2MetadataGeneratorCapabilityDescriptor
    implements CapabilityDescriptor
{

    public static final String ID = P2MetadataGeneratorCapability.ID;

    private final FormField repoOrGroup;

    public P2MetadataGeneratorCapabilityDescriptor()
    {
        repoOrGroup =
            new RepoOrGroupComboFormField( P2MetadataGeneratorConfiguration.REPO_OR_GROUP_ID, FormField.MANDATORY );
    }

    @Override
    public String id()
    {
        return ID;
    }

    @Override
    public String name()
    {
        return "P2 Metadata Generator capability";
    }

    @Override
    public List<FormField> formFields()
    {
        return Arrays.asList( repoOrGroup );
    }

    @Override
    public boolean isExposed()
    {
        return true;
    }

}
