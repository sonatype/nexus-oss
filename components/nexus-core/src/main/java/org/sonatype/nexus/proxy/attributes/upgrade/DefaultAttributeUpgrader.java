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

package org.sonatype.nexus.proxy.attributes.upgrade;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.util.LinearNumberSequence;
import org.sonatype.nexus.util.LowerLimitNumberSequence;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Component that handles upgrade of "legacy attribute storage". It does it by detecting it's presence, and firing the
 * rebuild attributes background task if needed. Finally, it leaves "marker" file to mark the fact upgrade did happen,
 * to not kick in on any subsequent reboot.
 *
 * @since 2.0
 */
@Component(role = AttributeUpgrader.class)
public class DefaultAttributeUpgrader
    extends AbstractLoggingComponent
    implements AttributeUpgrader, Disposable
{
  private static final String JMX_DOMAIN = "org.sonatype.nexus.proxy.attributes.upgrade";

  /**
   * The "switch" for performing upgrade (by bg thread), default is true (upgrade will happen).
   */
  private final boolean UPGRADE = SystemPropertiesHelper.getBoolean(getClass().getName() + ".upgrade", true);

  /**
   * The "switch" to enable "upgrade throttling" if needed (is critical to lessen IO bound problem with attributes).
   * Default is to 100 UPS to for use throttling. The "measure" is "UPS": item Upgrades Per Second. Reasoning is:
   * Central repository is currently 300k of artifacts, this would mean in "nexus world" 6x300k items (pom, jar,
   * maven-metadata and sha1/md5 hashes for those) if all of Central would be proxied by Nexus, which is not
   * plausible, so assume 50% of Central is present in cache (still is OVER-estimation!). Crawling 900k at 100 UPS
   * would take exactly 2.5 hour to upgrade it. Note: this is only the value used for unattended upgrade! This is
   * only
   * the initial value, that is still possible to "tune" (increase, decrease) over JMX! Possible values: -1 means no
   * throttling, will bash up to the Hardware limits, any other positive integer would mean a limit of UPS to not
   * reach over.
   */
  private final int UPGRADE_THROTTLE_UPS = SystemPropertiesHelper.getInteger(getClass().getName() + ".throttleUps",
      100);

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  @Requirement
  private RepositoryRegistry repositoryRegistry;

  private ObjectName jmxName;

  private int upgradeThrottleTps;

  private LowerLimitNumberSequence lowerLimitNumberSequence;

  private volatile AttributeUpgraderThread upgraderThread;

  public DefaultAttributeUpgrader() {
    this.upgradeThrottleTps = UPGRADE_THROTTLE_UPS;
    this.lowerLimitNumberSequence = new LowerLimitNumberSequence(new LinearNumberSequence(0, 1, 1, 0), 0);

    try {
      jmxName = ObjectName.getInstance(JMX_DOMAIN, "name", AttributeUpgrader.class.getSimpleName());
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      if (server.isRegistered(jmxName)) {
        getLogger().warn("MBean already registered; replacing: {}", jmxName);
        server.unregisterMBean(jmxName);
      }
      server.registerMBean(new DefaultAttributeUpgraderMBean(this), jmxName);
    }
    catch (Exception e) {
      jmxName = null;
      getLogger().warn("Problem registering MBean for: " + getClass().getName(), e);
    }
  }

  // ==

  @Override
  public void dispose() {
    shutdown();
    // kill the daemon thread
    if (isUpgradeRunning()) {
      upgraderThread.interrupt();
    }
  }

  // ==

  public void shutdown() {
    if (null != jmxName) {
      try {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        if (server.isRegistered(jmxName)) {
          server.unregisterMBean(jmxName);
        }
      }
      catch (final Exception e) {
        getLogger().warn("Problem unregistering MBean for: " + getClass().getName(), e);
      }
    }
  }

  protected File getLegacyAttributesDirectory() {
    return applicationConfiguration.getWorkingDirectory("proxy/attributes", false);
  }

  @Override
  public boolean isLegacyAttributesDirectoryPresent() {
    return getLegacyAttributesDirectory().isDirectory();
  }

  @Override
  public boolean isUpgradeNeeded() {
    return isLegacyAttributesDirectoryPresent() && !isUpgradeFinished();
  }

  @Override
  public boolean isUpgradeRunning() {
    return upgraderThread != null && upgraderThread.isAlive();
  }

  @Override
  public boolean isUpgradeFinished() {
    try {
      return isUpgradeDone(getLegacyAttributesDirectory(), null);
    }
    catch (IOException e) {
      // return true to prevent advancing in any aspect, but the error log will make a day for sysadmin
      getLogger().error("Unable to perform file read from legacy attributes directory: {}",
          getLegacyAttributesDirectory(), e);
      return true;
    }
  }

  @Override
  public int getGlobalAverageTps() {
    if (isUpgradeRunning()) {
      return upgraderThread.getThrottleController().getGlobalAverageTps();
    }
    else {
      return -1;
    }
  }

  @Override
  public int getGlobalMaximumTps() {
    if (isUpgradeRunning()) {
      return upgraderThread.getThrottleController().getGlobalMaximumTps();
    }
    else {
      return -1;
    }
  }

  @Override
  public int getLastSliceTps() {
    if (isUpgradeRunning()) {
      return upgraderThread.getThrottleController().getLastSliceTps();
    }
    else {
      return -1;
    }
  }

  @Override
  public long getCurrentSleepTime() {
    if (isUpgradeRunning()) {
      return upgraderThread.getThrottleController().getCurrentSleepTime();
    }
    else {
      return -1;
    }
  }

  public long getMinimumSleepTime() {
    return lowerLimitNumberSequence.getLowerLimit();
  }

  public void setMinimumSleepTime(long sleepTime) {
    lowerLimitNumberSequence.setLowerLimit(sleepTime);
  }

  @Override
  public int getLimiterTps() {
    if (isUpgradeRunning()) {
      return upgraderThread.getThrottleController().getLimiterTps();
    }
    else {
      return upgradeThrottleTps;
    }
  }

  @Override
  public void setLimiterTps(int limiterTps) {
    if (isUpgradeRunning()) {
      upgraderThread.getThrottleController().setLimiterTps(limiterTps);
    }
    else {
      upgradeThrottleTps = limiterTps;
    }
  }

  @Override
  public void upgradeAttributes() {
    upgradeAttributes(false);
  }

  @Override
  public synchronized void upgradeAttributes(final boolean force) {
    if (isUpgradeRunning()) {
      return;
    }

    if (!isLegacyAttributesDirectoryPresent()) {
      // file not found or not a directory, stay put to not create noise in logs (new or tidied up nexus
      // instance)
      getLogger().debug("Legacy attribute directory not present, no need for attribute upgrade.");
    }
    else {
      if (isUpgradeFinished()) {
        // nag the user to remove the directory
        getLogger().info(
            "Legacy attribute directory present, but is marked already as upgraded. Please delete, move or rename the \"{}\" directory.",
            getLegacyAttributesDirectory().getAbsolutePath());
      }
      else {
        if (force || UPGRADE) {
          if (force) {
            getLogger().info(
                "Legacy attribute directory present, and upgrade is needed and if forced. Starting background upgrade.");
          }
          else {
            getLogger().info(
                "Legacy attribute directory present, and upgrade is needed. Starting background upgrade.");
          }
          this.upgraderThread =
              new AttributeUpgraderThread(getLegacyAttributesDirectory(), repositoryRegistry,
                  upgradeThrottleTps, lowerLimitNumberSequence);
          this.upgraderThread.start();
        }
        else {
          // nag the user about explicit no-upgrade switch
          getLogger().info(
              "Legacy attribute directory present, but upgrade prevented by system property. Not upgrading it.");
        }
      }
    }
  }

  // ==

  private static final String MARKER_FILENAME = "README.txt";

  private static final String MARKER_TEXT =
      "Migration of legacy attributes finished.\nPlease delete, remove or rename this directory!";

  /**
   * Check if the repository with the given id has been marked as upgrade complete.
   *
   * @param attributesDirectory a directory where the marker file may be located
   * @param repoId              the repo id to check if upgrade is done, or blank ( null )
   * @return true if upgrade is done for specified repo or all repos if repoid is blank
   * @throws IOException problem detecting if upgrade is done
   * @todo make not static for better testability
   */
  protected static boolean isUpgradeDone(final File attributesDirectory, final String repoId)
      throws IOException
  {
    if (StringUtils.isBlank(repoId)) {
      final File markerFile = new File(attributesDirectory, MARKER_FILENAME);
      if (markerFile.exists()) {
        return StringUtils.equals(MARKER_TEXT, FileUtils.fileRead(markerFile));
      }
      else {
        return false;
      }
    }
    else {
      final File markerFile = new File(new File(attributesDirectory, repoId), MARKER_FILENAME);
      if (markerFile.exists()) {
        return StringUtils.equals(MARKER_TEXT, FileUtils.fileRead(markerFile));
      }
      else {
        return false;
      }
    }
  }

  /**
   * Mark a repo, or all repos if repoid is blank, as upgrade done.
   *
   * @param attributesDirectory directory where a marker file is written
   * @param repoId              the repo id to mark as upgrade done or blank (null)
   * @throws IOException problem recording if upgrade is done
   */
  protected static void markUpgradeDone(final File attributesDirectory, final String repoId)
      throws IOException
  {
    if (StringUtils.isBlank(repoId)) {
      FileUtils.fileWrite(new File(attributesDirectory, MARKER_FILENAME), MARKER_TEXT);
    }
    else {
      final File target = new File(new File(attributesDirectory, repoId), MARKER_FILENAME);
      // this step is needed if new repo added while upgrade not done: it will NOT have legacy attributes
      // as other reposes, that were present in old/upgraded instance
      target.getParentFile().mkdirs();
      FileUtils.fileWrite(target, MARKER_TEXT);
    }
  }
}
