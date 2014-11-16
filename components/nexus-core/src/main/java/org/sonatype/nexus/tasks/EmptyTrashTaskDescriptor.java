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

package org.sonatype.nexus.tasks;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;

import com.google.common.collect.Lists;

@Named
@Singleton
public class EmptyTrashTaskDescriptor
    extends TaskDescriptorSupport
{
  public EmptyTrashTaskDescriptor() {
    super(EmptyTrashTask.class, "Empty Trash");
  }

  public static final String OLDER_THAN_FIELD_ID = "EmptyTrashItemsOlderThan";

  private final NumberTextFormField olderThanField =
      new NumberTextFormField(
          OLDER_THAN_FIELD_ID,
          "Purge items older than (days)",
          "Set the number of days, to purge all items that were trashed before the given number of days.",
          FormField.OPTIONAL);

  private final FormField repoField = new RepositoryCombobox(
      TaskConfiguration.REPOSITORY_ID_KEY,
      "Repository",
      "Select the repository to empty the trash.",
      FormField.MANDATORY
  ).includeAnEntryForAllRepositories();

  public List<FormField> formFields() {
    List<FormField> fields = Lists.newArrayList();

    fields.add(repoField);
    fields.add(olderThanField);

    return fields;
  }
}
