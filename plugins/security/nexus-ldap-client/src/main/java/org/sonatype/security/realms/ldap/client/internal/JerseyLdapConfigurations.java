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
package org.sonatype.security.realms.ldap.client.internal;

import java.util.List;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.security.realms.ldap.api.dto.LdapServerListEntryDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerListResponse;
import org.sonatype.security.realms.ldap.api.dto.LdapServerOrderRequest;
import org.sonatype.security.realms.ldap.api.dto.LdapServerRequest;
import org.sonatype.security.realms.ldap.api.dto.XStreamInitalizer;
import org.sonatype.security.realms.ldap.client.Configuration;
import org.sonatype.security.realms.ldap.client.LdapConfigurations;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of LDAP server configurations.
 *
 * @since 3.0
 */
public class JerseyLdapConfigurations
    extends SubsystemSupport<JerseyNexusClient>
    implements LdapConfigurations
{
  public JerseyLdapConfigurations(final JerseyNexusClient nexusClient) {
    super(nexusClient);
    new XStreamInitalizer().initXStream(nexusClient.getXStream());
  }

  @Override
  public List<Configuration> get() {
    try {
      final LdapServerListResponse response = getNexusClient().serviceResource("ldap/servers")
          .get(LdapServerListResponse.class);
      final List<Configuration> result = Lists.newArrayList();
      for (LdapServerListEntryDTO entry : response.getData()) {
        result.add(get(entry.getId()));
      }
      return result;
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public Configuration get(final String id) {
    checkNotNull(id);
    try {
      final LdapServerRequest response = getNexusClient().serviceResource("ldap/servers/" + id)
          .get(LdapServerRequest.class);
      return new JerseyConfiguration(getNexusClient(), response.getData());
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public Configuration create() {
    return new JerseyConfiguration(getNexusClient());
  }

  @Override
  public void order(final List<String> ids) {
    checkNotNull(ids);
    try {
      final LdapServerOrderRequest request = new LdapServerOrderRequest();
      request.getData().addAll(ids);
      getNexusClient().serviceResource("ldap/server_order").put(request);
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public void clearCaches() {
    try {
      getNexusClient().serviceResource("ldap/clearcache").delete();
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }
}
