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

package org.sonatype.nexus.coreui

import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.scheduling.TaskState

/**
 * Task exchange object.
 *
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class TaskXO
{
  @NotEmpty(groups = [Update, Schedule])
  String id

  Boolean enabled

  @NotEmpty(groups = [Create, Update])
  String name

  @NotEmpty(groups = Create)
  String typeId

  String typeName
  TaskState status
  String statusDescription

  Date nextRun
  Date lastRun
  String lastRunResult
  Boolean runnable
  Boolean stoppable

  String alertEmail

  Map<String, String> properties

  @NotEmpty(groups = Create)
  String schedule

  Date startDate
  Integer[] recurringDays
  String cronExpression

  public interface Schedule
  {}
}
