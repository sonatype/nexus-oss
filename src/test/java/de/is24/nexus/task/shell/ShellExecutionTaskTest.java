package de.is24.nexus.task.shell;

import java.io.IOException;
import org.junit.Test;


public class ShellExecutionTaskTest {
  @Test
  public void shouldExecuteTask() throws Exception {
    ShellExecutionTask task = new ShellExecutionTask();
    task.setCommand("/bin/bash");
    task.doRun();
  }

  @Test(expected = IOException.class)
  public void shouldFailForOnNonZeroExitCode() throws Exception {
    ShellExecutionTask task = new ShellExecutionTask();
    task.setCommand("/bla/blup/foo");
    task.doRun();
  }
}
