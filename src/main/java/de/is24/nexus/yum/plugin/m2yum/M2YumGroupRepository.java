package de.is24.nexus.yum.plugin.m2yum;

import static java.util.Arrays.asList;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

import de.is24.nexus.yum.service.YumService;

@Component(role = GroupRepository.class, hint = M2YumGroupRepository.ID, instantiationStrategy = "per-lookup", description = "Maven2-Yum Repository Group")
public class M2YumGroupRepository extends M2GroupRepository {
  public static final String ID = "maven2yum";

  @Requirement(hint = M2YumContentClass.ID)
  private ContentClass contentClass;

  @Requirement
  private YumService yumService;

  private RepositoryKind repositoryKind;

  @Override
  public ContentClass getRepositoryContentClass() {
    return contentClass;
  }

  @Override
  public RepositoryKind getRepositoryKind() {
    if (repositoryKind == null) {
      repositoryKind = new DefaultRepositoryKind(GroupRepository.class, asList(new Class<?>[] { MavenGroupRepository.class,
          M2YumGroupRepository.class }));
    }
    return repositoryKind;
  }

  @Override
  public void addMemberRepositoryId(String repositoryId) throws NoSuchRepositoryException, InvalidGroupingException {
    super.addMemberRepositoryId(repositoryId);
    yumService.createGroupRepository(this);
  }

  @Override
  public void removeMemberRepositoryId(String repositoryId) {
    super.removeMemberRepositoryId(repositoryId);
    yumService.createGroupRepository(this);
  }

}
