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
package org.sonatype.security.events;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event fired by realm (usually contributed from plugin) when it is configured properly (for example, user edited and
 * saved usable configuration) and is ready for operation, it became operational and might be used. Realm referenced in
 * this event will be activated, if was inactive, and will be used by security.
 * <p>
 * <b>Warning: plugins/realms firing this event MUST FIRE the {@link RealmConfigurationUnusableEvent} event too, to
 * signal to security system the opposite event, their configuration was removed.</b>
 * 
 * @author cstamas
 * @since 2.7.0
 */
public class RealmConfigurationUsableEvent
{
  private final String realmId;

  public RealmConfigurationUsableEvent(final String realmId) {
    this.realmId = checkNotNull(realmId);
  }

  public String getRealmId() {
    return realmId;
  }
}
