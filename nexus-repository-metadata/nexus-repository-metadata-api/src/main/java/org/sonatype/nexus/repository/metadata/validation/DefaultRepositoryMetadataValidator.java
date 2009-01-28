package org.sonatype.nexus.repository.metadata.validation;

import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public class DefaultRepositoryMetadataValidator
    implements RepositoryMetadataValidator
{
    public void validate( RepositoryMetadata metadata )
        throws ValidationException
    {
        // also allow 3rd parties to "hook in" for validation.
    }

}
