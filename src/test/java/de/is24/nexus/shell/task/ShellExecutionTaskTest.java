package de.is24.nexus.shell.task;

import java.io.IOException;
import org.junit.Test;

import de.is24.nexus.shell.task.ShellExecutionTask;


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
