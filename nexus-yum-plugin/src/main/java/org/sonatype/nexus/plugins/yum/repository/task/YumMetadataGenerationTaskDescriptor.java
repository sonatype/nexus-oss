package org.sonatype.nexus.plugins.yum.repository.task;

import static org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask.PARAM_REPO_DIR;
import static org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask.PARAM_REPO_ID;
import static java.util.Arrays.asList;
import static org.sonatype.nexus.formfields.FormField.MANDATORY;
import static org.sonatype.nexus.formfields.FormField.OPTIONAL;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
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

  private final StringTextFormField outputField = new StringTextFormField(PARAM_REPO_DIR, "Optional Output Directory",
      "Directory which should contain the yum metadata after generation. If not set, yum will generate the metadata into the root directory of the selected repository.",
      OPTIONAL);

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
    return asList((FormField) repoField, (FormField) outputField);
  }

}
