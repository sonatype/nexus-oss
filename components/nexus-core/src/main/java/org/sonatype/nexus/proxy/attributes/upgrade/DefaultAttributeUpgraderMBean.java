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

import javax.management.StandardMBean;

/**
 * Simple exposure of AttributeUpgrader to JMX.
 *
 * @author cstamas
 * @since 2.0
 */
public class DefaultAttributeUpgraderMBean
    extends StandardMBean
    implements AttributeUpgraderMBean
{
  private final AttributeUpgrader attributeUpgrader;

  protected DefaultAttributeUpgraderMBean(final AttributeUpgrader attributeUpgrader) {
    super(AttributeUpgraderMBean.class, false);
    this.attributeUpgrader = attributeUpgrader;
  }

  @Override
  public boolean isLegacyAttributesDirectoryPresent() {
    return attributeUpgrader.isLegacyAttributesDirectoryPresent();
  }

  @Override
  public boolean isUpgradeNeeded() {
    return attributeUpgrader.isUpgradeNeeded();
  }

  @Override
  public boolean isUpgradeRunning() {
    return attributeUpgrader.isUpgradeRunning();
  }

  @Override
  public boolean isUpgradeFinished() {
    return attributeUpgrader.isUpgradeFinished();
  }

  @Override
  public int getGlobalAverageTps() {
    return attributeUpgrader.getGlobalAverageTps();
  }

  @Override
  public int getGlobalMaximumTps() {
    return attributeUpgrader.getGlobalMaximumTps();
  }

  @Override
  public int getLastSliceTps() {
    return attributeUpgrader.getLastSliceTps();
  }

  @Override
  public long getCurrentSleepTime() {
    return attributeUpgrader.getCurrentSleepTime();
  }

  public long getMinimumSleepTime() {
    return attributeUpgrader.getMinimumSleepTime();
  }

  public void setMinimumSleepTime(long sleepTime) {
    attributeUpgrader.setMinimumSleepTime(sleepTime);
  }

  @Override
  public int getLimiterTps() {
    return attributeUpgrader.getLimiterTps();
  }

  @Override
  public void setLimiterTps(int limiterTps) {
    attributeUpgrader.setLimiterTps(limiterTps);
  }

  @Override
  public void upgradeAttributes() {
    attributeUpgrader.upgradeAttributes(true);
  }
}
