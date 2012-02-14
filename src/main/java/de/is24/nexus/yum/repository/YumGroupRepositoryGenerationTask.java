package de.is24.nexus.yum.repository;

import static java.lang.String.format;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;

@Component(role = SchedulerTask.class, hint = YumGroupRepositoryGenerationTask.ID, instantiationStrategy = "per-lookup")
public class YumGroupRepositoryGenerationTask extends AbstractNexusTask<YumRepository> {

  public static final String ID = "YumGroupRepositoryGenerationTask";
  private GroupRepository groupRepository;

  public void setGroupRepository(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  @Override
  protected YumRepository doRun() throws Exception {
    return null;
  }

  @Override
  protected String getAction() {
    return "GENERATE_YUM_GROUP_REPOSITORY";
  }

  @Override
  protected String getMessage() {
    return format("Generate yum metadata for group repository '%s'", groupRepository.getId());
  }

}
