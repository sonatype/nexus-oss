package org.sonatype.nexus.plugins.repository;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.repository.HostedRepository;

@RepositoryType( pathPrefix = SimpleRepository.PATH_PREFIX )
public interface SimpleRepository
    extends HostedRepository
{
    String PATH_PREFIX = "simply";

    String sayHello();
}
