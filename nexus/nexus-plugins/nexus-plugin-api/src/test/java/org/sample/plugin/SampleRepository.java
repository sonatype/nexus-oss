package org.sample.plugin;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.repository.HostedRepository;

@RepositoryType(pathPrefix="sample")
public interface SampleRepository
    extends HostedRepository
{
    String boo();
}
