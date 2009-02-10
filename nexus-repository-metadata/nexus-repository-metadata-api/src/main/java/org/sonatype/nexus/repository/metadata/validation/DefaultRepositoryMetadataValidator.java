package org.sonatype.nexus.repository.metadata.validation;

import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public class DefaultRepositoryMetadataValidator
    implements RepositoryMetadataValidator
{
    public void validate( RepositoryMetadata metadata )
        throws ValidationException
    {
        metadata.setVersion( RepositoryMetadata.MODEL_VERSION );
    }

}
