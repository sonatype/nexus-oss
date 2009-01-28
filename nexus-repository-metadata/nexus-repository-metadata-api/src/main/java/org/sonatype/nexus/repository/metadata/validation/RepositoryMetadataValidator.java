package org.sonatype.nexus.repository.metadata.validation;

import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public interface RepositoryMetadataValidator
{
    void validate( RepositoryMetadata metadata )
        throws ValidationException;
}
