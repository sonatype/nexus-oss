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

package org.sonatype.nexus.index.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.tasks.AbstractScheduledTaskDescriptor;

public abstract class AbstractIndexTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{

  public static final String REPO_OR_GROUP_FIELD_ID = "repositoryId";

  public static final String RESOURCE_STORE_PATH_FIELD_ID = "resourceStorePath";

  private final FormField repoField;

  private final StringTextFormField resourceStorePathField = new StringTextFormField(RESOURCE_STORE_PATH_FIELD_ID,
      "Repository path",
      "Enter a repository path to run the task in recursively (ie. \"/\" for root or \"/org/apache\").",
      FormField.OPTIONAL);

  private String id;

  private String name;

  public AbstractIndexTaskDescriptor(String id, String name) {
    super();

    this.id = id;
    this.name = name;

    repoField = new RepositoryCombobox(
        REPO_OR_GROUP_FIELD_ID,
        "Repository",
        "Select the Maven repository to " + name.toLowerCase() + " the index.",
        FormField.MANDATORY
    ).includeAnEntryForAllRepositories()
        .includingAnyOfContentClasses(Maven2ContentClass.ID);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name + " Repositories Index";
  }

  @Override
  public List<FormField> formFields() {
    List<FormField> fields = new ArrayList<FormField>();

    fields.add(repoField);
    fields.add(resourceStorePathField);

    return fields;
  }

}