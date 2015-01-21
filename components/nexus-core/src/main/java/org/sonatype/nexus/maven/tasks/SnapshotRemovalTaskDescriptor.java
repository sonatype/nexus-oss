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
package org.sonatype.nexus.maven.tasks;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;

@Named
@Singleton
public class SnapshotRemovalTaskDescriptor
    extends TaskDescriptorSupport
{
  public static final String MIN_TO_KEEP_FIELD_ID = "minSnapshotsToKeep";

  public static final String KEEP_DAYS_FIELD_ID = "removeOlderThanDays";

  public static final String REMOVE_WHEN_RELEASED_FIELD_ID = "removeIfReleaseExists";

  public static final String GRACE_DAYS_AFTER_RELEASE_FIELD_ID = "graceDaysAfterRelease";

  public static final String DELETE_IMMEDIATELY = "deleteImmediately";

  public SnapshotRemovalTaskDescriptor() {
    super(SnapshotRemovalTask.class, "Remove Snapshots From Repository",
        new RepositoryCombobox(
            TaskConfiguration.REPOSITORY_ID_KEY,
            "Repository",
            "Select the Maven repository to remove snapshots",
            FormField.MANDATORY).includeAnEntryForAllRepositories().includingAnyOfContentClasses(
            Maven2ContentClass.ID).excludingAnyOfFacets(ProxyRepository.class),
        new NumberTextFormField(
            MIN_TO_KEEP_FIELD_ID,
            "Minimum snapshot count",
            "Minimum number of snapshots to keep for one GAV",
            FormField.MANDATORY),
        new NumberTextFormField(
            KEEP_DAYS_FIELD_ID,
            "Snapshot retention (days)",
            "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep",
            FormField.MANDATORY),
        new CheckboxFormField(
            REMOVE_WHEN_RELEASED_FIELD_ID,
            "Remove if released",
            "The job will purge all snapshots that have a corresponding released artifact (same version not including the -SNAPSHOT)",
            FormField.OPTIONAL),
        new NumberTextFormField(
            GRACE_DAYS_AFTER_RELEASE_FIELD_ID,
            "Grace period after release (days)",
            "The grace period (in days) that the task will not purge all snapshots that have a corresponding released artifact",
            FormField.OPTIONAL),
        new CheckboxFormField(
            DELETE_IMMEDIATELY,
            "Delete immediately",
            "The job will not move deleted items into the repository trash but delete immediately",
            FormField.OPTIONAL)
    );
  }
}
