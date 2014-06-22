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
package org.sonatype.nexus.analytics;

import javax.annotation.Nullable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to build {@link EventData} instances.
 *
 * @since 3.0
 */
public class EventDataBuilder
{
  private final EventData data = new EventData();

  public EventDataBuilder(final String type) {
    data.setType(type);
    data.setTimestamp(System.nanoTime());

    // capture the user and session ids if we can
    Subject subject = SecurityUtils.getSubject();
    if (subject != null) {
      Object principal = subject.getPrincipal();
      if (principal != null) {
        data.setUserId(principal.toString());
      }

      Session session = subject.getSession(false);
      if (session != null) {
        data.setSessionId(session.getId().toString());
      }
    }
  }

  public EventDataBuilder set(final String name, final @Nullable Object value) {
    checkNotNull(name);
    if (value == null) {
      data.getAttributes().put(name, null);
    }
    else {
      data.getAttributes().put(name, String.valueOf(value));
    }
    return this;
  }

  public EventData build() {
    data.setDuration(System.nanoTime() - data.getTimestamp());
    return data;
  }
}
