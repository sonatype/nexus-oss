package org.sonatype.nexus.plugins.yum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;


public class LogOutputStream extends OutputStream {
  private static final int BUF_SIZE = 3 * 1024;
  private final Logger log;
  private final boolean logLevelError;
  private boolean loggedErrors = false;

  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(BUF_SIZE);

  public LogOutputStream(Logger log, boolean logLevelError) {
    this.log = log;
    this.logLevelError = logLevelError;
  }

  @Override
  public void write(int b) throws IOException {
    if ((b == 10) || (buffer.size() == (BUF_SIZE - 1))) {
      log(buffer.toString());
      buffer.reset();
    } else {
      buffer.write(b);
    }
  }

  private void log(String string) {
    if (logLevelError) {
      log.error(string);
      loggedErrors = true;
    } else {
      if (string.contains("[ERROR]")) {
        loggedErrors = true;
      }
      log.info(string);
    }
  }

  public boolean hasLoggedErrors() {
    return loggedErrors;
  }
}
