/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.integrationtests;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: See if this still works, its been a long time IIUC since its been used

/**
 * Helper to profile testsuite with YourKit.
 *
 * @since 3.0
 */
public class ProfilerHelper
  extends ExternalResource
{
  private static final Logger log = LoggerFactory.getLogger(ProfilerHelper.class);

  private Object profiler;

  public void startProfiler() {
    Class<?> controllerClazz;
    try {
      controllerClazz = getClass().getClassLoader().loadClass("com.yourkit.api.Controller");
    }
    catch (Exception e) {
      log.info("Profiler not present");
      return;
    }

    try {
      profiler = controllerClazz.newInstance();

      captureSnapshot();
    }
    catch (Exception e) {
      log.warn("Failed to start profiler", e);
    }
  }

  public void captureSnapshot() {
    if (profiler != null) {
      log.info("Capturing snapshot");
      try {
        profiler.getClass().getMethod("forceGC").invoke(profiler);
        profiler.getClass().getMethod("captureMemorySnapshot").invoke(profiler);
      }
      catch (Exception e) {
        log.error("Failed to take snapshot", e);
      }
    }
  }

  @Override
  protected void before() throws Throwable {
    startProfiler();
  }
}
