/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.yum.plugin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.repository.GroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;

public class GroupRepositoryIT extends AbstractIntegrationTestCase {


  @Test
  public void shouldCreateAGroupRepository() throws Exception {
    final Repositories repositories = client().getSubsystem(Repositories.class);
    final String reponame = uniqueName();
    final GroupRepository groupRepo = repositories.create(GroupRepository.class, reponame);
    groupRepo.settings().setName(reponame);
    groupRepo.settings().setProvider("maven2yum");
    groupRepo.settings().setRepoType("group");
    groupRepo.settings().setExposed(true);
    assertThat(groupRepo.save().settings().getProvider(), is("maven2yum"));
  }

}
