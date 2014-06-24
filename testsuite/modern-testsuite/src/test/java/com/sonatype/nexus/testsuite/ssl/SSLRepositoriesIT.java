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

package com.sonatype.nexus.testsuite.ssl;

import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.sisu.siesta.common.validation.ValidationErrorsException;

import org.junit.Test;

import static com.sonatype.nexus.ssl.model.RepositoryTrustStoreKey.repositoryTrustStoreKey;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * ITs related to repositories keys / access.
 *
 * @since 1.0
 */
public class SSLRepositoriesIT
    extends SSLITSupport
{

  public SSLRepositoriesIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  /**
   * Verify repository trust store key.
   */
  @Test
  public void manageRepositoryTrustStoreKeys()
      throws Exception
  {
    final MavenProxyRepository proxyRepository =
        repositories().create(MavenProxyRepository.class, repositoryIdForTest())
            .asProxyOf("https://localhost:445")
            .doNotDownloadRemoteIndexes()
            .save();

    assertThat(truststore().isEnabledFor(repositoryTrustStoreKey(proxyRepository.id())), is(false));

    truststore().enableFor(repositoryTrustStoreKey(proxyRepository.id()));
    assertThat(truststore().isEnabledFor(repositoryTrustStoreKey(proxyRepository.id())), is(true));

    truststore().disableFor(repositoryTrustStoreKey(proxyRepository.id()));
    assertThat(truststore().isEnabledFor(repositoryTrustStoreKey(proxyRepository.id())), is(false));
  }

  /**
   * Verify repository trust store key cannot be enabled for an inexistent repository.
   */
  @Test
  public void repositoryTrustStoreKeysCannotBeEnabledForInexistentRepository()
      throws Exception
  {
    thrown.expect(ValidationErrorsException.class);

    truststore().enableFor(repositoryTrustStoreKey(uniqueName("repo")));
  }

  /**
   * Verify repository trust store key cannot be enabled for a non proxy repository.
   */
  @Test
  public void repositoryTrustStoreKeysCannotBeEnabledForNonProxyRepository()
      throws Exception
  {
    final MavenHostedRepository hostedRepository =
        repositories().create(MavenHostedRepository.class, repositoryIdForTest())
            .save();

    thrown.expect(ValidationErrorsException.class);

    truststore().enableFor(repositoryTrustStoreKey(hostedRepository.id()));
  }
}
