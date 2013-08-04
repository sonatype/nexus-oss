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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.RecreateAttributesWalker;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.proxy.walker.FixedRateWalkerThrottleController;
import org.sonatype.nexus.proxy.walker.FixedRateWalkerThrottleController.FixedRateWalkerThrottleControllerCallback;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.proxy.walker.WalkerThrottleController;
import org.sonatype.nexus.util.NumberSequence;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The background thread doing the actual attribute upgrades (moving them from legacy to LS attribute storage).
 *
 * @author cstamas
 * @since 2.0
 */
public class AttributeUpgraderThread
    extends Thread
    implements FixedRateWalkerThrottleControllerCallback
{

  protected static final int RUN_SLEEP_SECONDS = 5;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final File legacyAttributesDirectory;

  private final RepositoryRegistry repositoryRegistry;

  private final FixedRateWalkerThrottleController throttleController;

  public AttributeUpgraderThread(final File legacyAttributesDirectory, final RepositoryRegistry repositoryRegistry,
                                 final int limiterTps, final NumberSequence numberSequence)
  {
    this.legacyAttributesDirectory = legacyAttributesDirectory;
    this.repositoryRegistry = repositoryRegistry;
    // set throttle controller
    this.throttleController = new FixedRateWalkerThrottleController(limiterTps, numberSequence, this);
    // to have it clearly in thread dumps
    setName("LegacyAttributesUpgrader");
    // to not prevent sudden reboots (by user, if upgrading, and rebooting)
    setDaemon(true);
    // to not interfere much with other stuff (CPU wise)
    setPriority(Thread.MIN_PRIORITY);
  }

  public FixedRateWalkerThrottleController getThrottleController() {
    return throttleController;
  }

  /**
   * Determine if a repository should be upgraded.
   * <br/>
   * A repo should not be upgraded if it is Group or Shadow faceted
   *
   * @param repo The repo to check for upgrade
   * @return true if repo should be upgraded
   */
  protected boolean shouldUpgradeRepository(Repository repo) {
    // NEXUS-5099: Skipping shadows
    return !repo.getRepositoryKind().isFacetAvailable(GroupRepository.class)
        && !repo.getRepositoryKind().isFacetAvailable(MavenShadowRepository.class);
  }

  /**
   * @see DefaultAttributeUpgrader#isUpgradeDone(java.io.File, String)
   */
  protected boolean isUpgradeDone(final String repoId)
      throws IOException
  {
    return DefaultAttributeUpgrader.isUpgradeDone(legacyAttributesDirectory, repoId);
  }

  /**
   * @see DefaultAttributeUpgrader#markUpgradeDone(java.io.File, String)
   */
  protected void markUpgradeDone(final String repoId)
      throws IOException
  {
    DefaultAttributeUpgrader.markUpgradeDone(legacyAttributesDirectory, repoId);
  }

  /**
   * Sleeps the calling thread {@link #RUN_SLEEP_SECONDS}
   */
  protected void throttleRun()
      throws InterruptedException
  {
    TimeUnit.SECONDS.sleep(RUN_SLEEP_SECONDS);
  }

  @Override
  public void run() {
    try {
      // defer actual start a bit to not start prematurely (ie. nexus boot not done yet, let it "calm down")
      throttleRun();
    }
    catch (InterruptedException e) {
      // thread will die off
      return;
    }

    try {
      if (!isUpgradeDone(null)) {
        boolean weHadSkippedRepositories = false;
        final long started = System.currentTimeMillis();
        List<Repository> reposes = repositoryRegistry.getRepositories();
        for (Repository repo : reposes) {
          if (shouldUpgradeRepository(repo)) {
            if (LocalStatus.IN_SERVICE.equals(repo.getLocalStatus())) {
              if (!isUpgradeDone(repo.getId())) {
                try {
                  logger.info("Upgrading legacy attributes of repository {}.",
                      RepositoryStringUtils.getHumanizedNameString(repo));

                  ResourceStoreRequest req = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT);
                  req.getRequestContext().put(WalkerThrottleController.CONTEXT_KEY,
                      throttleController);
                  req.getRequestContext().put(RecreateAttributesWalker.FORCE_ATTRIBUTE_RECREATION,
                      Boolean.FALSE);
                  req.getRequestContext().put(RecreateAttributesWalker.LEGACY_ATTRIBUTES_ONLY,
                      Boolean.TRUE);
                  repo.recreateAttributes(req, null);
                  markUpgradeDone(repo.getId());
                  logger.info("Upgrade of legacy attributes of repository {} done.",
                      RepositoryStringUtils.getHumanizedNameString(repo));
                }
                catch (WalkerException e) {
                  weHadSkippedRepositories = true;
                  logger.error(
                      "Problems during legacy attribute upgrade of repository {}, skipping it. Please fix the problems and retry the upgrade.",
                      RepositoryStringUtils.getHumanizedNameString(repo), e);
                }
                catch (IOException e) {
                  logger.error("Unable to perform file write to legacy attributes directory: {}",
                      legacyAttributesDirectory.getAbsolutePath());

                  throw e;
                }
              }
              else {
                logger.debug(
                    "Skipping legacy attributes of repository {}, already marked as upgraded.",
                    RepositoryStringUtils.getHumanizedNameString(repo));
              }
            }
            else {
              weHadSkippedRepositories = true;
              logger.info(
                  "Deferring legacy attributes upgrade of repository {}, is out of service. Upgrade will be automatically retried once repository will be in service, and upgrade restarted (next restart of Nexus or manually invoked over JMX).",
                  RepositoryStringUtils.getHumanizedNameString(repo));
            }
          }
        }

        final String totalRuntimeString =
            DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - started);

        if (!weHadSkippedRepositories) {
          try {
            // mark instance as upgraded
            markUpgradeDone(null);
          }
          catch (IOException e) {
            logger.error("Unable to perform file write to legacy attributes directory: {}",
                legacyAttributesDirectory.getAbsolutePath());

            throw e;
          }

          logger.info(
              "Legacy attribute directory upgrade finished without any errors. Please delete, move or rename the \"{}\" folder. Total runtime {}",
              legacyAttributesDirectory.getAbsolutePath(), totalRuntimeString);
        }
        else {
          logger.info(
              "Legacy attribute directory upgrade was partially completed. Please see prior log messages for details. Total runtime {}",
              totalRuntimeString);
        }
      }
    }
    catch (IOException e) {
      // if we are here, that means that one of the isUpgradeDone or markUpgradeDone puked.
      // Write failures are already noted above (see catches around markUpgradeDone).
      logger.error(
          "Stopping legacy attributes upgrade because of file read/write related problems. Please fix the problems and retry the upgrade.",
          e);
    }
  }

  @Override
  public void onAdjustment(final FixedRateWalkerThrottleController controller) {
    logger.debug(
        "Current speed {} upgrades/sec, with average {} upgrade/sec (is limited to {} upgrades/sec), currently sleeping {}ms per upgrade.",
        new Object[]{
            controller.getLastSliceTps(), controller.getGlobalAverageTps(), controller.getLimiterTps(),
            controller.getCurrentSleepTime()
        });
  }
}
