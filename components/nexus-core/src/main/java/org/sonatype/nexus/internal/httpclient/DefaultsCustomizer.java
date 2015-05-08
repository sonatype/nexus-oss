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
package org.sonatype.nexus.internal.httpclient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.httpclient.HttpClientPlan;
import org.sonatype.sisu.goodies.common.ByteSize;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.common.Time;

import com.google.common.net.HttpHeaders;
import org.apache.http.client.config.CookieSpecs;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Applies defaults to {@link HttpClientPlan}.
 *
 * @since 3.0
 */
@Named
@Singleton
@SuppressWarnings("PackageAccessibility") // FIXME: httpclient usage is producing lots of OSGI warnings in IDEA
public class DefaultsCustomizer
  extends ComponentSupport
  implements HttpClientPlan.Customizer
{
  private final UserAgentGenerator userAgentGenerator;

  private final Time requestTimeout;

  private final Time keepAliveDuration;

  private final ByteSize bufferSize;

  @Inject
  public DefaultsCustomizer(
      final UserAgentGenerator userAgentGenerator,
      @Named("${nexus.httpclient.requestTimeout:-30s}") final Time requestTimeout,
      @Named("${nexus.httpclient.keepAliveDuration:-30s}") final Time keepAliveDuration,
      @Named("${nexus.httpclient.bufferSize:-8k}") final ByteSize bufferSize)
  {
    this.userAgentGenerator = checkNotNull(userAgentGenerator);

    this.requestTimeout = checkNotNull(requestTimeout);
    log.debug("Request timeout: {}", requestTimeout);

    this.keepAliveDuration = checkNotNull(keepAliveDuration);
    log.debug("Keep-alive duration: {}", keepAliveDuration);

    this.bufferSize = checkNotNull(bufferSize);
    log.debug("Buffer-size: {}", bufferSize);
  }

  @Override
  public void customize(final HttpClientPlan plan) {
    checkNotNull(plan);

    plan.getHeaders().put(HttpHeaders.USER_AGENT, userAgentGenerator.generate());

    plan.getClient().setKeepAliveStrategy(new NexusConnectionKeepAliveStrategy(keepAliveDuration.toMillis()));

    plan.getConnection().setBufferSize(bufferSize.toBytesI());

    plan.getRequest().setConnectionRequestTimeout(requestTimeout.toMillisI());
    plan.getRequest().setCookieSpec(CookieSpecs.IGNORE_COOKIES);
    plan.getRequest().setExpectContinueEnabled(false);
    plan.getRequest().setStaleConnectionCheckEnabled(false);
  }
}
