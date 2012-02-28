package de.is24.nexus.yum.plugin.m2yum;

import static de.is24.nexus.yum.repository.RepositoryUtils.getBaseDir;
import static java.util.Arrays.asList;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

import de.is24.nexus.yum.service.YumService;

@Component(role = GroupRepository.class, hint = M2YumGroupRepository.ID, instantiationStrategy = "per-lookup", description = "Maven2-Yum Repository Group")
public class M2YumGroupRepository extends M2GroupRepository {
  public static final String ID = "maven2yum";

  @Requirement(hint = M2YumContentClass.ID)
  private ContentClass contentClass;

  @Requirement
  private YumService yumService;

  @Requirement
  private RepositoryRegistry repositoryRegistry;

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
    final boolean isNewRpmRepository = isNewRepo(repositoryId) && isRpmRepo(repositoryId);
    super.addMemberRepositoryId(repositoryId);
    if (isNewRpmRepository) {
      yumService.createGroupRepository(this);
    }
  }

  private boolean isRpmRepo(String repositoryId) throws NoSuchRepositoryException {
    final Repository repository = repositoryRegistry.getRepository(repositoryId);
    try {
      if (repository.getRepositoryKind().isFacetAvailable(MavenHostedRepository.class)
          && new File(getBaseDir(repository), "repodata/repomd.xml").exists()) {
        return true;
      }
    } catch (Exception e) {
    }
    return false;
  }

  private boolean isNewRepo(String repositoryId) {
    return !getMemberRepositoryIds().contains(repositoryId);
  }

  @Override
  public void removeMemberRepositoryId(String repositoryId) {
    super.removeMemberRepositoryId(repositoryId);
    try {
      if (isRpmRepo(repositoryId)) {
        yumService.createGroupRepository(this);
      }
    } catch (NoSuchRepositoryException e) {
      throw new RuntimeException("Could not detect rpm repository.", e);
    }
  }

}
