package org.sonatype.nexus.plugins.p2.repository;

import org.sonatype.nexus.proxy.item.StorageItem;

public interface P2RepositoryAggregator
{

    void addConfiguration( final P2RepositoryAggregatorConfiguration configuration );

    void removeConfiguration( final P2RepositoryAggregatorConfiguration configuration );

    P2RepositoryAggregatorConfiguration getConfiguration( final String repositoryId );

    void updateP2Artifacts( StorageItem item );

    void removeP2Artifacts( StorageItem item );

    void updateP2Metadata( StorageItem item );

    void removeP2Metadata( StorageItem item );

    void scanAndRebuild( String repositoryId );

    void scanAndRebuild();

}
