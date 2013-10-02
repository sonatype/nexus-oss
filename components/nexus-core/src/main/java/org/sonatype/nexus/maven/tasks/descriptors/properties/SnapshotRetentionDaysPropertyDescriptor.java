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

package org.sonatype.nexus.maven.tasks.descriptors.properties;

import javax.enterprise.inject.Typed;
import javax.inject.Named;

import org.sonatype.nexus.tasks.descriptors.properties.AbstractNumberPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Named("SnapshotRetentionDays")
@Typed(ScheduledTaskPropertyDescriptor.class)
public class SnapshotRetentionDaysPropertyDescriptor
    extends AbstractNumberPropertyDescriptor
{
  public static final String ID = "removeOlderThanDays";

  public SnapshotRetentionDaysPropertyDescriptor() {
    setHelpText(
        "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep.");
    setRequired(false);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "Snapshot retention (days)";
  }

}
