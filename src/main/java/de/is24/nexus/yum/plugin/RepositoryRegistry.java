package de.is24.nexus.yum.plugin;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import de.is24.nexus.yum.plugin.impl.MavenRepositoryInfo;


public interface RepositoryRegistry {
  void unregisterRepository(Repository repository);

  MavenRepository findRepositoryForId(String repositoryId);

  MavenRepositoryInfo findRepositoryInfoForId(String repositoryId);

  boolean isRegistered(Repository repository);

  void registerRepository(MavenRepository repository);

}
