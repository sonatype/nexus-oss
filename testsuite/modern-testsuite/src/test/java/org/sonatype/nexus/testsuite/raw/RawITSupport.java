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
package org.sonatype.nexus.testsuite.raw;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.raw.internal.RawGroupRecipe;
import org.sonatype.nexus.repository.raw.internal.RawHostedRecipe;
import org.sonatype.nexus.repository.raw.internal.RawProxyRecipe;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Support class for raw ITs.
 */
public class RawITSupport
    extends RepositoryTestSupport
{
  @NotNull
  protected Configuration hostedConfig(final String name) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(RawHostedRecipe.NAME);
    config.setOnline(true);
    config.attributes("storage").set("writePolicy", WritePolicy.ALLOW.toString());
    return config;
  }

  @NotNull
  protected Configuration proxyConfig(final String name, final String remoteUrl) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(RawProxyRecipe.NAME);
    config.setOnline(true);

    final NestedAttributesMap proxy = config.attributes("proxy");
    proxy.set("remoteUrl", remoteUrl);
    proxy.set("artifactMaxAge", 5);

    final NestedAttributesMap negativeCache = config.attributes("negativeCache");
    negativeCache.set("enabled", true);
    negativeCache.set("timeToLive", new Integer(100000));

    return config;
  }

  @NotNull
  protected Configuration groupConfig(final String name, final String... memberNames) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(RawGroupRecipe.NAME);
    config.setOnline(true);

    NestedAttributesMap group = config.attributes("group");
    group.set("memberNames", asList(memberNames));

    return config;
  }

  @NotNull
  protected RawClient client(final Repository repository) throws Exception {
    checkNotNull(repository);
    return new RawClient(clientBuilder().build(),
        clientContext(),
        repositoryBaseUrl(repository).toURI());
  }
}
