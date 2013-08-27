/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.test.utils;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineRunner
{

  private static final Logger LOG = LoggerFactory.getLogger(CommandLineRunner.class);

  private final StringBuilder buffer = new StringBuilder();


  public int executeAndWait(Commandline cli) throws CommandLineException, InterruptedException {
    Process p = null;
    StreamPumper outPumper = null;
    StreamPumper errPumper = null;

    StreamConsumer out = new StreamConsumer()
    {
      public void consumeLine(String line) {
        buffer.append(line).append("\n");
      }
    };

    try {
      LOG.debug("executing: " + cli.toString());
      p = cli.execute();

      // we really don't need the stream pumps... but just in case... and if your into that whole sys-out style of
      // debugging this is for you...
      outPumper = new StreamPumper(p.getInputStream(), out);
      errPumper = new StreamPumper(p.getErrorStream(), out);

      outPumper.setPriority(Thread.MIN_PRIORITY + 1);
      errPumper.setPriority(Thread.MIN_PRIORITY + 1);

      outPumper.start();
      errPumper.start();

      return p.waitFor();

    }
    finally {
      if (outPumper != null) {
        outPumper.close();
      }

      if (errPumper != null) {
        errPumper.close();
      }
    }
  }

  public String getConsoleOutput() {
    return this.buffer.toString();
  }

}
