package org.sonatype.nexus.plugins.p2.repository;

import org.sonatype.nexus.proxy.item.StorageItem;

public interface P2MetadataGenerator
{

    void addConfiguration( final P2MetadataGeneratorConfiguration configuration );

    void removeConfiguration( final P2MetadataGeneratorConfiguration configuration );

    P2MetadataGeneratorConfiguration getConfiguration( final String repositoryId );

    void generateP2Metadata( StorageItem item );

    void removeP2Metadata( StorageItem item );

}
