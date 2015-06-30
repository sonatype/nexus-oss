/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal.security;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.security.authc.AuthenticationTokenFactory;
import org.sonatype.nexus.security.authc.HttpHeaderAuthenticationToken;
import org.sonatype.nexus.security.authc.HttpHeaderAuthenticationTokenFactorySupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AuthenticationTokenFactory} that creates {@link NugetAuthenticationToken}s if a configured HTTP header
 * is present.
 *
 * @since 3.0
 */
@Named
@Singleton
public class NugetAuthenticationTokenFactory
    extends HttpHeaderAuthenticationTokenFactorySupport
{
  private static final Logger log = LoggerFactory.getLogger(NugetAuthenticationTokenFactory.class);

  private static final String HEADER_NAME = "X-NuGet-ApiKey";

  @Inject
  public NugetAuthenticationTokenFactory() {
    log.info("{} created", getClass().getSimpleName());
  }

  @Override
  protected List<String> getHttpHeaderNames() {
    return Collections.singletonList(HEADER_NAME);
  }

  @Override
  protected HttpHeaderAuthenticationToken createToken(String headerName, String headerValue, final String host) {
    return new NugetAuthenticationToken(headerName, headerValue, host);
  }
}
