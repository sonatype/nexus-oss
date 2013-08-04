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

package org.sonatype.appcontext.publisher;

import java.util.Map.Entry;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A publisher that publishes Application Context back to to System properties, probably prefixed with keyPrefix, to
 * make it available for other system components like loggers, caches, etc.
 *
 * @author cstamas
 */
public class SystemPropertiesEntryPublisher
    implements EntryPublisher
{
  /**
   * The prefix to be used to prefix keys ("prefix.XXX"), if set.
   */
  private final String keyPrefix;

  /**
   * Flag to force publishing. Otherwise, the system property will be set only if does not exists.
   */
  private final boolean override;

  /**
   * Constructs a publisher without prefix, will publish {@code key=values} with keys as is in context.
   */
  public SystemPropertiesEntryPublisher(final boolean override) {
    this.keyPrefix = null;
    this.override = override;
  }

  /**
   * Constructs a publisher with prefix, will publish context with {@code prefix.key=value}.
   *
   * @throws NullPointerException if {@code keyPrefix} is null
   */
  public SystemPropertiesEntryPublisher(final String keyPrefix, final boolean override) {
    this.keyPrefix = Preconditions.checkNotNull(keyPrefix);
    this.override = override;
  }

  public void publishEntries(final AppContext context) {
    for (Entry<String, Object> entry : context.entrySet()) {
      String key = entry.getKey();
      String value = String.valueOf(entry.getValue());

      // adjust the key name and put it back to System properties
      String sysPropKey = keyPrefix == null ? key : keyPrefix + key;

      if (override || System.getProperty(sysPropKey) == null) {
        System.setProperty(sysPropKey, (String) value);
      }
    }
  }
}
