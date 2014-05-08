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

package org.sonatype.nexus.scheduling;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The base interface for all Tasks used in Nexus.
 *
 * @author cstamas
 */
public interface NexusTask<T>
    extends Callable<T>
{
  /**
   * Returns a unique ID of the task.
   */
  String getId();

  /**
   * Sets the unique ID of the task.
   */
  void setId(String id);

  /**
   * Returns a name of the task.
   */
  String getName();

  /**
   * Sets the name of the task.
   */
  void setName(String name);

  /**
   * Is task enabled?
   */
  boolean isEnabled();

  /**
   * Sets the enabled flag of task.
   */
  void setEnabled(boolean enabled);

  /**
   * Is this task visible on UI? This is decided by implementation, is not dynamic.
   */
  boolean isExposed();

  /**
   * Should an alert email be sent?
   */
  boolean shouldSendAlertEmail();

  /**
   * Returns the email address to which an email should be sent in case of task failure.<br/>
   * If the alert email is not set (null or empty) no email should be sent.
   */
  String getAlertEmail();

  /**
   * Sets the email address to which an email should be sent in case of task failure.
   */
  void setAlertEmail(String alertEmail);

  /**
   * "Raw" parameter setter. If value is {@code null} the key mapping is removed from parameters map.
   */
  void addParameter(String key, String value);

  /**
   * "Raw" parameter getter.
   */
  String getParameter(String key);

  /**
   * Returns the parameter backing map, any change done here is reflected in task parameters.
   */
  Map<String, String> getParameters();

  /**
   * Returns a view (copy) of task "public" (non-private) parameters. Changes done here are not reflected in task
   * parameters.
   */
  Map<String, String> getPublicParameters();
}
