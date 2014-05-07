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
package org.sonatype.nexus.rapture;

/**
 * RaptureSettings.
 *
 * @since 3.0
 */
public class RaptureSettings
{

  public static final String DEFAULT_TITLE = "Sonatype Nexus";

  public static final boolean DEFAULT_DEBUG_ALLOWED = true;

  public static final int DEFAULT_STATUS_INTERVAL_AUTHENTICATED = 5; // seconds

  public static final int DEFAULT_STATUS_INTERVAL_ANONYMOUS = 60; // seconds

  public static final int DEFAULT_SESSION_TIMEOUT = 30; // minutes

  private boolean debugAllowed = DEFAULT_DEBUG_ALLOWED;

  private int statusIntervalAuthenticated = DEFAULT_STATUS_INTERVAL_AUTHENTICATED;

  private int statusIntervalAnonymous = DEFAULT_STATUS_INTERVAL_ANONYMOUS;

  private int sessionTimeout = DEFAULT_SESSION_TIMEOUT;

  private String title;

  public boolean isDebugAllowed() {
    return debugAllowed;
  }

  public void setDebugAllowed(final boolean debugAllowed) {
    this.debugAllowed = debugAllowed;
  }

  public int getStatusIntervalAuthenticated() {
    return statusIntervalAuthenticated;
  }

  public void setStatusIntervalAuthenticated(final int statusInterval) {
    this.statusIntervalAuthenticated = statusInterval;
  }

  public int getStatusIntervalAnonymous() {
    return statusIntervalAnonymous;
  }

  public void setStatusIntervalAnonymous(final int statusInterval) {
    this.statusIntervalAnonymous = statusInterval;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(final int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
