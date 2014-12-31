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
 * {@link NtlmAuthenticationConfig} marshaller.
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
  public void marshall(final AuthenticationConfig config, final NestedAttributesMap attributes) {
    checkNotNull(config);
    checkNotNull(attributes);
    NtlmAuthenticationConfig cfg = (NtlmAuthenticationConfig) config;
    attributes.set("username", cfg.getUsername());
    attributes.set("password", cfg.getPassword());
    attributes.set("ntlmHost", cfg.getNtlmHost());
    attributes.set("ntlmDomain", cfg.getNtlmDomain());
  }

  @Override
  public AuthenticationConfig unmarshall(final NestedAttributesMap attributes) {
    checkNotNull(attributes);
    NtlmAuthenticationConfig result = new NtlmAuthenticationConfig();
    result.setUsername(attributes.get("username", String.class));
    result.setPassword(attributes.get("password", String.class));
    result.setNtlmHost(attributes.get("ntlmHost", String.class));
    result.setNtlmDomain(attributes.get("ntlmDomain", String.class));
    return result;
  }
}
