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
 package org.sonatype.nexus.plugins.yum.plugin.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Test;


public class VersionizedRepositoryViewIT extends AbstractNexusTestBase {
  static final String SERVICE_BASE_URL = "http://localhost:8080/nexus/service/local";

  @Test
  public void shouldGetEmpytViewForRepository() throws Exception {
    assertThat(executeGet("/yum/repos/snapshots/1.0.0-SNAPSHOT/"),
      containsString("<a href=\"repodata/\">repodata/</a>"));
  }
}
