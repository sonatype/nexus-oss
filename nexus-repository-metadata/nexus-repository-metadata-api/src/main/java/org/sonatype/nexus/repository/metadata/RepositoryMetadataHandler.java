package org.sonatype.nexus.repository.metadata;

import java.io.IOException;

import org.sonatype.nexus.repository.metadata.model.OrderedMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public interface RepositoryMetadataHandler
{
    RepositoryMetadata readRepositoryMetadata( MetadataRequest request )
        throws IOException;

    void writeRepositoryMetadata( MetadataRequest request, RepositoryMetadata metadata )
        throws IOException;

    OrderedMirrorMetadata fetchOrderedMirrorMetadata( String url, RawTransport transport )
        throws IOException;
}
