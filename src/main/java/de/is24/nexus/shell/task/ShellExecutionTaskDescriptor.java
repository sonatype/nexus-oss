package de.is24.nexus.shell.task;

import static java.util.Arrays.asList;
import static org.sonatype.nexus.formfields.FormField.MANDATORY;
import java.util.List;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;


@Component(
  role = ScheduledTaskDescriptor.class, hint = "ShellExecutionTask", description = ShellExecutionTaskDescriptor.NAME
)
public class ShellExecutionTaskDescriptor extends AbstractScheduledTaskDescriptor {
  public static final String ID = "ShellExecutionTask";
  public static final String NAME = "Shell Execution Task";
  public static final String COMMAND_FIELD_ID = "shellTaskCommand";

  private final StringTextFormField commandField = new StringTextFormField(
    COMMAND_FIELD_ID,
    "Command to execute in the shell",
    "This should be the complete command with arguments to be executed in the shell. It should be exactly one command nothing more. No shell script code is allowed.",
    MANDATORY);

  public String getId() {
    return ID;
  }

  public String getName() {
    return NAME;
  }

  @Override
  public List<FormField> formFields() {
    return asList((FormField) commandField);
  }

}
