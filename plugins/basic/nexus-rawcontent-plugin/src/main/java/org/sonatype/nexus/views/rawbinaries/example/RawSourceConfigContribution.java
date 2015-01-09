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
package org.sonatype.nexus.views.rawbinaries.example;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.nexus.component.source.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.config.ComponentSourceConfigContributor;
import org.sonatype.nexus.component.source.config.ComponentSourceConfigStore;
import org.sonatype.nexus.component.source.http.ConnectionConfig;
import org.sonatype.nexus.component.source.http.HttpClientConfig;
import org.sonatype.nexus.component.source.http.HttpClientConfigMarshaller;
import org.sonatype.nexus.views.rawbinaries.source.RawComponentSourceFactory;

import com.google.common.collect.Maps;

/**
 * Contributes an example binary source for search.maven.org.
 *
 * @since 3.0
 */
@Named("rawSource")
@Singleton
public class RawSourceConfigContribution
    implements ComponentSourceConfigContributor
{
  public static final String SOURCE_NAME = "binary-source";

  public static final String INTERNAL_ID = "foo_2f32wdf23r";

  private final HttpClientConfigMarshaller httpClientConfigMarshaller;

  @Inject
  public RawSourceConfigContribution(final HttpClientConfigMarshaller httpClientConfigMarshaller)
  {
    this.httpClientConfigMarshaller = httpClientConfigMarshaller;
  }

  @Override
  public void contributeTo(final ComponentSourceConfigStore store) throws IOException {
    if (store.get(SOURCE_NAME) == null) {
      Map<String, Object> config = Maps.newHashMap();
      config.put(RawComponentSourceFactory.REMOTE_URL_PARAM, "http://search.maven.org/");
      config.putAll(httpClientConfigMarshaller.toMap(
          new HttpClientConfig().withConnectionConfig(new ConnectionConfig()
                  .withTimeout(500)
                  .withRetries(5)
                  .withUserAgentCustomisation("NX3")
          )
      ));


      store.add(new ComponentSourceConfig(
          new ComponentSourceId(SOURCE_NAME, INTERNAL_ID),
          RawComponentSourceFactory.NAME,
          config
      ));
    }
  }
}
