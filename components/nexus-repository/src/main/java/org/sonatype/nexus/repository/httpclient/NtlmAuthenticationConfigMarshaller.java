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

package org.sonatype.nexus.repository.httpclient;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.goodies.common.ComponentSupport;

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
  public void toMap(final AuthenticationConfig config, final NestedAttributesMap configMap) {
    checkNotNull(config);
    checkNotNull(configMap);
    NtlmAuthenticationConfig cfg = (NtlmAuthenticationConfig) config;
    configMap.set("username", cfg.getUsername());
    configMap.set("password", cfg.getPassword());
    configMap.set("ntlmHost", cfg.getNtlmHost());
    configMap.set("ntlmDomain", cfg.getNtlmDomain());
  }

  @Override
  public AuthenticationConfig fromMap(final NestedAttributesMap configMap) {
    checkNotNull(configMap);
    NtlmAuthenticationConfig result = new NtlmAuthenticationConfig();
    result.setUsername(configMap.get("username", String.class));
    result.setPassword(configMap.get("password", String.class));
    result.setNtlmHost(configMap.get("ntlmHost", String.class));
    result.setNtlmDomain(configMap.get("ntlmDomain", String.class));
    return result;
  }

}
