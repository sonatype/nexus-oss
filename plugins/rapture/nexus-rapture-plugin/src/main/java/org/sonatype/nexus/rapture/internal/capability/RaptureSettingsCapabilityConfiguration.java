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

package org.sonatype.nexus.rapture.internal.capability;

import java.util.Map;

import org.sonatype.nexus.rapture.RaptureSettings;

import com.google.common.collect.Maps;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NonNls;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configuration adapter for {@link RaptureSettingsCapability}.
 *
 * @since 3.0
 */
public class RaptureSettingsCapabilityConfiguration
    extends RaptureSettings
{

  @NonNls
  public static final String DEBUG_ALLOWED = "debugAllowed";

  @NonNls
  public static final String STATUS_INTERVAL = "statusInterval";

  @NonNls
  public static final String SESSION_TIMEOUT = "sessionTimeout";

  @NonNls
  public static final String TITLE = "title";

  public RaptureSettingsCapabilityConfiguration() {
    this(Maps.<String, String>newHashMap());
  }

  public RaptureSettingsCapabilityConfiguration(final Map<String, String> properties) {
    checkNotNull(properties);
    setDebugAllowed(parseBoolean(properties.get(DEBUG_ALLOWED), DEFAULT_DEBUG_ALLOWED));
    setStatusInterval(parseInteger(properties.get(STATUS_INTERVAL), DEFAULT_STATUS_INTERVAL));
    setSessionTimeout(parseInteger(properties.get(SESSION_TIMEOUT), DEFAULT_SESSION_TIMEOUT));
    setTitle(parseString(properties.get(TITLE), DEFAULT_TITLE));
  }

  public Map<String, String> asMap() {
    final Map<String, String> props = Maps.newHashMap();
    props.put(DEBUG_ALLOWED, Boolean.toString(isDebugAllowed()));
    props.put(STATUS_INTERVAL, Integer.toString(getStatusInterval()));
    props.put(SESSION_TIMEOUT, Integer.toString(getSessionTimeout()));
    props.put(TITLE, getTitle());
    return props;
  }

  private boolean parseBoolean(final String value, final boolean defaultValue) {
    if (!isEmpty(value)) {
      return Boolean.parseBoolean(value);
    }
    return defaultValue;
  }

  private int parseInteger(final String value, final int defaultValue) {
    if (!isEmpty(value)) {
      return Integer.parseInt(value);
    }
    return defaultValue;
  }

  private String parseString(final String value, final String defaultValue) {
    if (!isEmpty(value)) {
      return value;
    }
    return defaultValue;
  }

  private boolean isEmpty(final String value) {
    return StringUtils.isEmpty(value);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "title=" + getTitle()
        + ", debugAllowed=" + isDebugAllowed()
        + ", statusInterval=" + getStatusInterval()
        + ", sessionTimeout=" + getSessionTimeout()
        + "}";
  }
}
