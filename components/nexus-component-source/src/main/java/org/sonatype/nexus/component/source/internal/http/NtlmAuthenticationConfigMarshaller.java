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
package org.sonatype.nexus.component.source.internal.http;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.http.AuthenticationConfig;
import org.sonatype.nexus.component.source.http.AuthenticationConfigMarshaller;
import org.sonatype.nexus.component.source.http.NtlmAuthenticationConfig;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link NtlmAuthenticationConfig} {@link AuthenticationConfigMarshaller}.
 *
 * @since 3.0
 */
@Named(NtlmAuthenticationConfig.TYPE)
@Singleton
public class NtlmAuthenticationConfigMarshaller
    extends ComponentSupport
    implements AuthenticationConfigMarshaller
{

  @Override
  public Map<String, Object> toMap(final AuthenticationConfig config) {
    checkNotNull(config, "config");
    NtlmAuthenticationConfig cfg = (NtlmAuthenticationConfig) config;
    Map<String, Object> configMap = Maps.newHashMapWithExpectedSize(4);
    configMap.put("username", cfg.getUsername());
    configMap.put("password", cfg.getPassword());
    configMap.put("ntlmHost", cfg.getNtlmHost());
    configMap.put("ntlmDomain", cfg.getNtlmDomain());
    return configMap;
  }

  @Override
  public AuthenticationConfig fromMap(final Map<String, Object> configMap) {
    checkNotNull(configMap, "config map");
    return new NtlmAuthenticationConfig()
        .withUsername((String) configMap.get("username"))
        .withPassword((String) configMap.get("password"))
        .withNtlmHost((String) configMap.get("ntlmHost"))
        .withNtlmDomain((String) configMap.get("ntlmDomain"));
  }

}
