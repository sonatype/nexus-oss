package de.is24.nexus.task.shell;

import static de.is24.nexus.task.shell.ShellExecutionTaskDescriptor.COMMAND_FIELD_ID;
import static de.is24.nexus.yum.execution.ExecutionUtil.execCommand;
import static java.lang.String.format;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;


@Component(role = SchedulerTask.class, hint = ShellExecutionTaskDescriptor.ID, instantiationStrategy = "per-lookup")
public class ShellExecutionTask extends AbstractNexusTask<Object> {
  private static final String SYSTEM_ACTION = "EXECUTE_SHELL_SCRIPT";

  @Override
  protected Object doRun() throws Exception {
    if (isValidCommand()) {
      final int exitCode = execCommand(getCommand());
      if (exitCode != 0) {
        throw new RuntimeException(format("Execution of command '%s' faild with exit code %d.", getCommand(),
            exitCode));
      }
    }

    return null;
  }

  private boolean isValidCommand() {
    return (getCommand() != null) && StringUtils.isNotBlank(getCommand());
  }

  @Override
  protected String getAction() {
    return SYSTEM_ACTION;
  }

  @Override
  protected String getMessage() {
    return "Executing shell script : " + getCommand();
  }

  public String getCommand() {
    return getParameters().get(COMMAND_FIELD_ID);
  }

  public void setCommand(String command) {
    getParameters().put(COMMAND_FIELD_ID, command);
  }
}
