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
package org.sonatype.nexus.features.internal;

import java.io.File;
import java.io.IOException;

import org.apache.karaf.features.internal.BundleManager;
import org.apache.karaf.region.persist.RegionsPersistence;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;

public class ReferenceBundleManager
    extends BundleManager
{
  private static final File SYSTEM_REPO = new File(System.getProperty("karaf.base", ".") + File.separatorChar
      + System.getProperty("karaf.default.repository", "system")).getAbsoluteFile();

  public ReferenceBundleManager(BundleContext ctx) {
    super(ctx);
  }

  public ReferenceBundleManager(BundleContext ctx, RegionsPersistence regions) {
    super(ctx, regions);
  }

  public ReferenceBundleManager(BundleContext ctx, RegionsPersistence regions, long timeout) {
    super(ctx, regions, timeout);
  }

  @Override
  public BundleInstallerResult installBundleIfNeeded(String bundleLocation, int startLevel, String regionName)
      throws IOException, BundleException
  {
    try {
      if (bundleLocation.startsWith("mvn:")) {
        File systemPath = getSystemPath(bundleLocation.substring(4).split("/"));
        if (systemPath.isFile()) {
          Bundle b = getBundleContext().installBundle("reference:" + systemPath.toURI());
          if (startLevel > 0) {
            b.adapt(BundleStartLevel.class).setStartLevel(startLevel);
          }
          return new BundleInstallerResult(b, true);
        }
      }
    }
    catch (Exception e) {
      // fall-through...
    }
    return super.installBundleIfNeeded(bundleLocation, startLevel, regionName);
  }

  private static File getSystemPath(String[] gav) {

    String artifactPath = gav[0].replace('.', '/') + '/' + gav[1] + '/' + gav[2];
    String artifactFile = gav[1] + '-' + gav[2] + ".jar";

    return new File(SYSTEM_REPO, artifactPath + '/' + artifactFile);
  }
}
