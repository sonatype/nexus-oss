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

package org.sonatype.nexus.error.reporting;

import org.sonatype.nexus.configuration.Configurable;

import org.codehaus.plexus.swizzle.IssueSubmissionException;

/**
 * An error reporting component.
 */
public interface ErrorReportingManager
    extends Configurable
{
  boolean isEnabled();

  void setEnabled(boolean value);

  /**
   * @return the URL of the JIRA instance the manager connects to.
   */
  String getJIRAUrl();

  /**
   * @param url the URL of the JIRA instance the manager connects to.
   */
  void setJIRAUrl(String url);

  /**
   * @return the key of the JIRA project new issues will be filed in.
   */
  String getJIRAProject();

  /**
   * @param pkey the key of the JIRA project new issues will be filed in.
   */
  void setJIRAProject(String pkey);

  /**
   * @return the username to use for connections.
   */
  String getJIRAUsername();

  /**
   * @param username the username to use for connections.
   */
  void setJIRAUsername(String username);

  /**
   * @return the password to use for connections.
   */
  String getJIRAPassword();

  /**
   * @param password the password to use for connections.
   */
  void setJIRAPassword(String password);

  // ==

  /**
   * File an issue based on the given error report request.
   */
  ErrorReportResponse handleError(ErrorReportRequest request)
      throws IssueSubmissionException;

  ErrorReportResponse handleError(ErrorReportRequest request, String jiraUsername, String jiraPassword)
      throws IssueSubmissionException;

}
