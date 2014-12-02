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

package org.sonatype.nexus.plugins.p2.repository.updatesite;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.plugins.p2.repository.UpdateSiteProxyRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;

@Named
@Singleton
public class UpdateSiteMirrorTaskDescriptor
    extends TaskDescriptorSupport<UpdateSiteMirrorTask>
{
  public static final String FORCE_MIRROR_FIELD_ID = "ForceMirror";

  public UpdateSiteMirrorTaskDescriptor()
  {
    super(UpdateSiteMirrorTask.class, "Mirror Eclipse Update Site",
        new RepositoryCombobox(
            TaskConfiguration.REPOSITORY_ID_KEY,
            "Repository",
            "Select Eclipse Update Site repository to assign to this task.",
            FormField.MANDATORY
        ).includeAnEntryForAllRepositories().includingAnyOfFacets(UpdateSiteProxyRepository.class,
            GroupRepository.class),
        new CheckboxFormField(
            FORCE_MIRROR_FIELD_ID, "Force mirror",
            "Mirror eclipse update site content even if site.xml did not change.", FormField.OPTIONAL)
    );
  }
}
