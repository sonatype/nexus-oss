/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.auth;

import java.util.Date;

/**
 * Abstract helper class for security related events. It carries the "minimal" subset of authc/authz events: the client
 * information ("who").
 *
 * @author cstamas
 * @since 2.0
 */
public abstract class AbstractSecurityEvent
{
  private final ClientInfo clientInfo;

  private final Date date;

  public AbstractSecurityEvent(final Object sender, final ClientInfo clientInfo) {
    this.clientInfo = clientInfo;
    this.date = new Date();
  }

  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  public Date getEventDate() {
    return date;
  }
}
