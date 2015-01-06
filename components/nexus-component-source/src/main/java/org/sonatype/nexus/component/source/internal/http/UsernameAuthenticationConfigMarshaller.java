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
package org.sonatype.nexus.component.source.internal.http;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.http.AuthenticationConfig;
import org.sonatype.nexus.component.source.http.AuthenticationConfigMarshaller;
import org.sonatype.nexus.component.source.http.UsernameAuthenticationConfig;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link UsernameAuthenticationConfig} {@link AuthenticationConfigMarshaller}.
 *
 * @since 3.0
 */
@Named(UsernameAuthenticationConfig.TYPE)
@Singleton
public class UsernameAuthenticationConfigMarshaller
    extends ComponentSupport
    implements AuthenticationConfigMarshaller
{

  @Override
  public Map<String, Object> toMap(final AuthenticationConfig config) {
    checkNotNull(config, "config");
    UsernameAuthenticationConfig cfg = (UsernameAuthenticationConfig) config;
    Map<String, Object> configMap = Maps.newHashMapWithExpectedSize(2);
    configMap.put("username", cfg.getUsername());
    configMap.put("password", cfg.getPassword());
    return configMap;
  }

  @Override
  public AuthenticationConfig fromMap(final Map<String, Object> configMap) {
    checkNotNull(configMap, "config map");
    return new UsernameAuthenticationConfig()
        .withUsername((String) configMap.get("username"))
        .withPassword((String) configMap.get("password"));
  }

}
