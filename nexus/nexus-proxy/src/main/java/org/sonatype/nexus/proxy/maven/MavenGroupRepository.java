package org.sonatype.nexus.proxy.maven;

public interface MavenGroupRepository
    extends MavenRepository
{
    boolean isMergeMetadata();

    void setMergeMetadata( boolean mergeMetadata );
}
