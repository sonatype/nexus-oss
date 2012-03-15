package de.is24.nexus.yum.repository.task;

import static de.is24.nexus.yum.repository.task.YumMetadataGenerationTask.PARAM_REPO_ID;
import static java.util.Arrays.asList;
import static org.sonatype.nexus.formfields.FormField.MANDATORY;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;


@Component(
role = ScheduledTaskDescriptor.class, hint = YumMetadataGenerationTask.ID, description = YumMetadataGenerationTaskDescriptor.NAME
)
public class YumMetadataGenerationTaskDescriptor extends AbstractScheduledTaskDescriptor {
  public static final String NAME = "Yum Createrepo Task";

  private final RepoComboFormField repoField = new RepoComboFormField(PARAM_REPO_ID, "Repostiory for createrepo",
      "Maven Repository for which the yum metadata is generated via createrepo.",
    MANDATORY);

  @Override
  public String getId() {
    return YumMetadataGenerationTask.ID;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<FormField> formFields() {
    return asList((FormField) repoField);
  }

}
