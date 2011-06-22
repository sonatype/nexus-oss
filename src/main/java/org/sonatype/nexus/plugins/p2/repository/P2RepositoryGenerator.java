package org.sonatype.nexus.plugins.p2.repository;

public interface P2RepositoryGenerator
{

    void addConfiguration( final P2RepositoryGeneratorConfiguration configuration );

    void removeConfiguration( final P2RepositoryGeneratorConfiguration configuration );

    P2RepositoryGeneratorConfiguration getConfiguration( final String repositoryId );

}
