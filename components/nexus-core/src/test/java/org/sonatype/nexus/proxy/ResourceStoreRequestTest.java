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

package org.sonatype.nexus.proxy;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.*;

public class ResourceStoreRequestTest
    extends TestSupport
{
  @Test
  public void plainPathCreation()
      throws Exception
  {
    final ResourceStoreRequest request = new ResourceStoreRequest("/some/path");
    assertThat(request.getRequestPath(), equalTo("/some/path"));
    assertThat(request.isRequestLocalOnly(), equalTo(false));
    assertThat(request.isRequestRemoteOnly(), equalTo(false));
    assertThat(request.isRequestAsExpired(), equalTo(false));
    assertThat(request.isRequestGroupLocalOnly(), equalTo(false));
    assertThat(request.isRequestGroupMembersOnly(), equalTo(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void corruptPathCreationMaven()
      throws Exception
  {
    new ResourceStoreRequest("/some/path/${maven.placeholder}/bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void corruptPathCreationNexus6501_case1()
      throws Exception
  {
    new ResourceStoreRequest(
        "/org/springframework/spring-context/maven-metadata.xml (4 KB at 2.5 KB/sec)");
  }

  @Test(expected = IllegalArgumentException.class)
  public void corruptPathCreationNexus6501_case2()
      throws Exception
  {
    new ResourceStoreRequest(
        "/co/orange/crm/paym/services/ear/crm-paym-subscription-contract-ear/${env.CCASE_VERSION_STRING}/crm-paym-subscription-contract-ear-${env.CCASE_VERSION_STRING}.pom");
  }
}
