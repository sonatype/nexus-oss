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
package org.sonatype.nexus.yum.internal.task;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;

import static org.sonatype.nexus.formfields.FormField.MANDATORY;
import static org.sonatype.nexus.formfields.FormField.OPTIONAL;
import static org.sonatype.nexus.yum.internal.task.GenerateMetadataTask.PARAM_FORCE_FULL_SCAN;
import static org.sonatype.nexus.yum.internal.task.GenerateMetadataTask.PARAM_REPO_DIR;
import static org.sonatype.nexus.yum.internal.task.GenerateMetadataTask.PARAM_SINGLE_RPM_PER_DIR;

/**
 * @since yum 3.0
 */
@Named
@Singleton
public class GenerateMetadataTaskDescriptor
    extends TaskDescriptorSupport<GenerateMetadataTask>
{

  public static final String NAME = "Yum: Generate Metadata";

  public GenerateMetadataTaskDescriptor()
  {
    super(GenerateMetadataTask.class, NAME,
        new RepositoryCombobox(
            TaskConfiguration.REPOSITORY_ID_KEY,
            "Repository for createrepo",
            "Maven Repository for which the yum metadata is generated via createrepo.",
            MANDATORY
        ).includingAnyOfFacets(MavenRepository.class).excludingAnyOfFacets(GroupRepository.class),
        new StringTextFormField(
            PARAM_REPO_DIR,
            "Optional Output Directory",
            "Directory which should contain the yum metadata after generation."
                + " If not set, yum will generate the metadata into the root directory of the selected repository.",
            OPTIONAL
        ),
        new CheckboxFormField(
            PARAM_SINGLE_RPM_PER_DIR,
            "Single RPM per directory",
            "Only process one RPM per directory",
            OPTIONAL
        ).withInitialValue(true),
        new CheckboxFormField(
            PARAM_FORCE_FULL_SCAN,
            "Full Rebuild",
            "Forces a full rebuild and does not use the cached RPM file list.",
            OPTIONAL
        ).withInitialValue(false)
    );
  }
}
