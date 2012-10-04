/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
