package de.is24.nexus.yum.plugin;

import de.is24.nexus.yum.plugin.impl.MavenRepositoryInfo;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plugin.Managed;


@Managed
public interface RepositoryRegistry {
  String DEFAULT_BEAN_NAME = "repositoryRegistry";

  void unregisterRepository(Repository repository);

  MavenRepository findRepositoryForId(String repositoryId);

  MavenRepositoryInfo findRepositoryInfoForId(String repositoryId);

  boolean isRegistered(Repository repository);

  void registerRepository(MavenRepository repository);

}
