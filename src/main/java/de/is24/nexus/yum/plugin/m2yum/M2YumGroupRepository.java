package de.is24.nexus.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;

@Component(role = GroupRepository.class, hint = M2YumGroupRepository.ID, instantiationStrategy = "per-lookup", description = "Maven2-Yum Repository Group")
public class M2YumGroupRepository extends M2GroupRepository {
  public static final String ID = "maven2yum";

  @Requirement(hint = M2YumContentClass.ID)
  private ContentClass contentClass;

  @Override
  public ContentClass getRepositoryContentClass() {
    return contentClass;
  }

}
