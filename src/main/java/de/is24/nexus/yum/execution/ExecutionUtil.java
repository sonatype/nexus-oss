package de.is24.nexus.yum.execution;

import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ExecutionUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ExecutionUtil.class);

  public static int execCommand(String command) throws IOException {
    LOG.info("Execute command : {}", command);

    CommandLine cmdLine = CommandLine.parse(command);
    DefaultExecutor executor = new DefaultExecutor();
    executor.setStreamHandler(new PumpStreamHandler());

    int exitValue = executor.execute(cmdLine);
    LOG.info("Execution finished with exit code : {}", exitValue);
    return exitValue;
  }
}
