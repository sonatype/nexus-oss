package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.proxy.repository.GroupRepository;

public interface MavenGroupRepository
    extends MavenRepository, GroupRepository
{
    boolean isMergeMetadata();

    void setMergeMetadata( boolean mergeMetadata );
}
