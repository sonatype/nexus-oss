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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;

@Named
@Singleton
public class EvictUnusedProxiedItemsTaskDescriptor
    extends TaskDescriptorSupport
{
  public static final String OLDER_THAN_FIELD_ID = "evictOlderCacheItemsThen";

  public EvictUnusedProxiedItemsTaskDescriptor() {
    super(EvictUnusedProxiedItemsTask.class, "Evict Unused Proxied Items From Repository Caches",
        new RepositoryCombobox(
            TaskConfiguration.REPOSITORY_ID_KEY,
            "Repository",
            "Select the proxy repository to evict unused items.",
            FormField.MANDATORY
        ).includeAnEntryForAllRepositories()
            .includingAnyOfFacets(ProxyRepository.class, GroupRepository.class),
        new NumberTextFormField(
            OLDER_THAN_FIELD_ID,
            "Evict items older than (days)",
            "Set the number of days, to evict all unused proxied items that were not used the given number of days.",
            FormField.MANDATORY)
    );
  }
}
