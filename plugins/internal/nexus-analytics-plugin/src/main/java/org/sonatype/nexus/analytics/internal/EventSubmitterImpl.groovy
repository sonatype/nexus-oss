/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.analytics.internal

import com.sonatype.analytics.client.AnalyticsClientBuilder
import com.sun.jersey.api.client.Client
import com.sun.jersey.client.apache4.ApacheHttpClient4
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler
import org.sonatype.nexus.analytics.EventSubmitter
import org.sonatype.nexus.apachehttpclient.Hc4Provider

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * {@link EventSubmitter} implementation.
 *
 * @since 2.8
 */
@Named
@Singleton
class EventSubmitterImpl
implements EventSubmitter
{

  private final String analyticsServiceUrl

  private final Hc4Provider hc4Provider

  @Inject
  EventSubmitterImpl(final @Named('${nexus.analytics.service.url:-http://analytics.sonatype.com}') String analyticsServiceUrl,
                     final Hc4Provider hc4Provider)
  {
    assert analyticsServiceUrl
    assert hc4Provider

    this.analyticsServiceUrl = analyticsServiceUrl
    this.hc4Provider = hc4Provider
  }

  @Override
  void submit(final File export) {
    AnalyticsClientBuilder.createFor(analyticsServiceUrl, client()).upload(export);
  }

  private Client client() {
    return new ApacheHttpClient4(new ApacheHttpClient4Handler(hc4Provider.createHttpClient(), null, false))
  }

}
