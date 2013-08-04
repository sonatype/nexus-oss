/*
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

package org.sonatype.nexus.capabilities.client.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.capabilities.client.Filter;
import org.sonatype.nexus.capabilities.client.exceptions.CapabilityFactoryNotAvailableException;
import org.sonatype.nexus.capabilities.client.exceptions.MultipleCapabilitiesFoundException;
import org.sonatype.nexus.capabilities.client.spi.JerseyCapabilityFactory;
import org.sonatype.nexus.capabilities.model.XStreamConfigurator;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.rest.jersey.ContextAwareUniformInterfaceException;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Jersey based Capabilities Nexus Client Subsystem implementation.
 *
 * @since 2.1
 */
public class JerseyCapabilities
    extends SubsystemSupport<JerseyNexusClient>
    implements Capabilities
{

  private static final Logger LOG = LoggerFactory.getLogger(JerseyCapabilities.class);

  private static final Filter ALL = null;

  public static final String NO_RESPONSE_BODY = null;

  private final Set<JerseyCapabilityFactory> capabilityFactories;

  public JerseyCapabilities(final JerseyNexusClient nexusClient,
                            final Set<JerseyCapabilityFactory> capabilityFactories)
  {
    super(nexusClient);
    XStreamConfigurator.configureXStream(nexusClient.getXStream());
    this.capabilityFactories = checkNotNull(capabilityFactories);
  }

  @Override
  public Capability create(final String type) {
    return findFactoryOf(type).create(getNexusClient());
  }

  @Override
  public Capability get(final String id) {
    try {
      return convert(
          getNexusClient()
              .serviceResource(pathStatus(id))
              .get(CapabilityStatusResponseResource.class)
              .getData()
      );
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(new CapabilityAwareUniformInterfaceException(e.getResponse(), id));
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public Collection<Capability> get() {
    LOG.debug("Retrieving all capabilities");
    return queryFor(ALL);
  }

  @Override
  public Collection<Capability> get(final Filter filter) {
    LOG.debug("Retrieving all capabilities using filter '{}'", checkNotNull(filter).toQueryMap());
    return queryFor(filter);
  }

  @Override
  public Capability getUnique(final Filter filter)
      throws MultipleCapabilitiesFoundException, NexusClientNotFoundException
  {
    final Collection<Capability> capabilities = get(filter);
    if (capabilities.size() == 0) {
      throw new NexusClientNotFoundException(
          String.format("No capability found matching filter '%s'", filter),
          NO_RESPONSE_BODY
      );
    }
    if (capabilities.size() > 1) {
      throw new MultipleCapabilitiesFoundException(filter, capabilities);
    }
    return capabilities.iterator().next();
  }

  @Override
  public <C extends Capability> C create(final Class<C> type)
      throws CapabilityFactoryNotAvailableException
  {
    return findFactoryOf(type).create(getNexusClient());
  }

  @Override
  public <C extends Capability> C get(final Class<C> type, final String id) {
    checkNotNull(type);
    final Capability capability = get(id);
    if (!type.isAssignableFrom(capability.getClass())) {
      throw new ClassCastException(
          String.format(
              "Expected an '%s' but found that capability is an '%s'",
              type.getName(), capability.getClass().getName()
          )
      );
    }
    return type.cast(capability);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends Capability> Collection<C> get(final Class<C> type, final Filter filter) {
    LOG.debug("Retrieving all capabilities of type {}", type.getName());
    final Collection<Capability> capabilities = queryFor(filter);
    for (final Capability capability : capabilities) {
      if (!type.isAssignableFrom(capability.getClass())) {
        throw new ClassCastException(
            String.format(
                "Expected an '%s' but found that capability is an '%s'",
                type.getName(), capability.getClass().getName()
            )
        );
      }
    }
    return (Collection<C>) capabilities;
  }

  @Override
  public <C extends Capability> C getUnique(final Class<C> type, final Filter filter)
      throws MultipleCapabilitiesFoundException, NexusClientNotFoundException, ClassCastException
  {
    checkNotNull(type);
    final Capability capability = getUnique(filter);
    if (!type.isAssignableFrom(capability.getClass())) {
      throw new ClassCastException(
          String.format(
              "Expected an '%s' but found that capability is an '%s'",
              type.getName(), capability.getClass().getName()
          )
      );
    }
    return type.cast(capability);
  }

  @SuppressWarnings("unchecked")
  private <C extends Capability> JerseyCapabilityFactory<C> findFactoryOf(final Class<C> type) {
    for (final JerseyCapabilityFactory factory : capabilityFactories) {
      if (factory.canCreate((Class<Capability>) type)) {
        LOG.debug(
            "Using factory {} for capability type {}",
            factory.getClass().getName(), type.getName()
        );
        return (JerseyCapabilityFactory<C>) factory;
      }
    }
    throw new CapabilityFactoryNotAvailableException((Class<Capability>) type);
  }

  private JerseyCapabilityFactory findFactoryOf(final String type) {
    checkNotNull(type);
    for (final JerseyCapabilityFactory factory : capabilityFactories) {
      if (factory.canCreate(type)) {
        LOG.debug(
            "Using factory {} for type '{}'",
            factory.getClass().getName(), type
        );
        return factory;
      }
    }
    LOG.debug(
        "Using factory {} for type '{}'",
        JerseyGenericCapabilityFactory.class.getName(), type
    );
    return new JerseyGenericCapabilityFactory(type);
  }

  private Collection<Capability> queryFor(final Filter filter) {
    final CapabilitiesListResponseResource resource;
    try {
      if (filter != null) {
        resource = getNexusClient()
            .serviceResource("capabilities", filter.toQueryMap())
            .get(CapabilitiesListResponseResource.class);
      }
      else {
        resource = getNexusClient()
            .serviceResource("capabilities")
            .get(CapabilitiesListResponseResource.class);
      }
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }

    return Collections2.transform(resource.getData(), new Function<CapabilityListItemResource, Capability>()
    {
      @Override
      public Capability apply(@Nullable final CapabilityListItemResource input) {
        return convert(input);
      }
    });
  }

  private Capability convert(final CapabilityListItemResource resource) {
    if (resource == null) {
      return null;
    }
    return findFactoryOf(resource.getTypeId()).create(getNexusClient(), resource);
  }

  public static String path(final String id) {
    try {
      return "capabilities/" + URLEncoder.encode(id, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  public static String pathStatus(final String id) {
    checkNotNull(id);
    try {
      return "capabilities/" + URLEncoder.encode(id, "UTF-8") + "/status";
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  public static class CapabilityAwareUniformInterfaceException
      extends ContextAwareUniformInterfaceException
  {

    private final String id;

    public CapabilityAwareUniformInterfaceException(final ClientResponse response, final String id) {
      super(response);
      this.id = id;
    }

    @Override
    public String getMessage(final int status) {
      if (status == Response.Status.NOT_FOUND.getStatusCode()) {
        return String.format("Capability with id '%s' was not found", id);
      }
      return null;
    }
  }

}
