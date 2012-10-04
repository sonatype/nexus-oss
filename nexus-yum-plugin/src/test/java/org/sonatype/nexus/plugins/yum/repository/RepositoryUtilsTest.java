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
 package org.sonatype.nexus.plugins.yum.repository;

import static org.sonatype.nexus.plugins.yum.repository.RepositoryUtils.getBaseDir;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.sonatype.nexus.proxy.repository.Repository;


public class RepositoryUtilsTest {
  @Test
  public void shouldConvertRepositoryLocalUrlToFile() throws Exception {
    assertConvertion("file:/hallo/world", "/hallo/world");
    assertConvertion("/hallo/world", "/hallo/world");
  }

  private void assertConvertion(String localUrl, String expected) throws Exception {
    Repository repo = createMock(Repository.class);
    expect(repo.getLocalUrl()).andReturn(localUrl).anyTimes();
    replay(repo);
    assertThat(getBaseDir(repo).getAbsolutePath(), is(expected));
  }
}
