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

package org.sonatype.nexus.quartz.internal.capability;

import java.util.Map;

import org.sonatype.nexus.capability.support.CapabilityConfigurationSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link SchedulerCapability} configuration.
 *
 * @since 3.0
 */
public class SchedulerCapabilityConfiguration
    extends CapabilityConfigurationSupport
{
  public static final String ACTIVE = "active";

  public static final boolean DEFAULT_ACTIVE = true;

  public static final String THREAD_POOL_SIZE = "threadPoolSize";

  public static final int DEFAULT_THREAD_POOL_SIZE = 20;

  private boolean active;

  private int threadPoolSize;

  public SchedulerCapabilityConfiguration(final Map<String, String> properties) {
    checkNotNull(properties);

    this.active = Boolean.valueOf(properties.get(ACTIVE));
    this.threadPoolSize = Integer.parseInt(properties.get(THREAD_POOL_SIZE));
  }

  public boolean isActive() { return active; }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public Map<String, String> asMap() {
    Map<String, String> props = Maps.newHashMap();
    props.put(ACTIVE, String.valueOf(active));
    props.put(THREAD_POOL_SIZE, String.valueOf(threadPoolSize));
    return props;
  }

  @Override
  public String toString() {
    return "SchedulerCapabilityConfiguration{" +
        "threadPoolSize='" + threadPoolSize + '\'' +
        ", active='" + active + '\'' +
        '}';
  }
}
