/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.karaf;

import java.io.File;

import org.apache.felix.utils.properties.Properties;
import org.apache.karaf.main.ConfigProperties;
import org.apache.karaf.main.lock.Lock;

/**
 * Nexus implementation of Karaf's {@link Lock} that exits this process if another process has the lock.
 * 
 * @since 3.0
 */
public class NexusFileLock
    implements Lock
{
  private final String dataDir;

  private final LockFile lockFile;

  public NexusFileLock(Properties properties) {
    dataDir = properties.getProperty(ConfigProperties.PROP_KARAF_DATA);
    lockFile = new LockFile(new File(dataDir, "lock"));
  }

  public boolean lock() {
    if (!lockFile.lock()) {
      // logging is not configured yet, so use console
      System.err.println("Nexus data directory already in use: " + dataDir);
      System.exit(-1);
    }
    return true;
  }

  public void release() {
    lockFile.release();
  }

  public boolean isAlive() {
    return true;
  }
}
