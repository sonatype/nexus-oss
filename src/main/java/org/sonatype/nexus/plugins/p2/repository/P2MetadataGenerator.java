package org.sonatype.nexus.plugins.p2.repository;


public interface P2MetadataGenerator
{

    void addConfiguration( final P2MetadataGeneratorConfiguration configuration );

    void removeConfiguration( final P2MetadataGeneratorConfiguration configuration );

    P2MetadataGeneratorConfiguration getConfiguration( final String repositoryId );

}
