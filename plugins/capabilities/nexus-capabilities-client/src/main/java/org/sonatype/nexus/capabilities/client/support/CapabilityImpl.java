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

package org.sonatype.nexus.capabilities.client.support;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.capabilities.client.spi.CapabilityClient;
import org.sonatype.nexus.client.rest.support.EntitySupport;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Jersey based {@link Capability}.
 *
 * @since 2.2
 */
public class CapabilityImpl<C extends Capability<C>>
    extends EntitySupport<C, CapabilityListItemResource>
    implements Capability<C>
{

  private final CapabilityClient client;

  public CapabilityImpl(final CapabilityClient client, final String type) {
    super(null);
    this.client = checkNotNull(client, "client");
    settings().withTypeId(type);
  }

  public CapabilityImpl(final CapabilityClient client, final CapabilityListItemResource settings) {
    super(settings.getId(), settings);
    this.client = checkNotNull(client, "client");
  }

  @Override
  public String id() {
    return settings().getId();
  }

  @Override
  protected CapabilityListItemResource createSettings(final String id) {
    final CapabilityListItemResource resource = new CapabilityListItemResource();
    resource.setId(id);
    resource.setEnabled(true);
    return resource;
  }

  @Override
  protected CapabilityListItemResource doGet() {
    return client.getStatus(id()).getData();
  }

  @Override
  protected CapabilityListItemResource doCreate() {
    final CapabilityRequestResource request = new CapabilityRequestResource();
    request.setData(settingsAsResource());
    return client.post(request).getData();
  }

  @Override
  protected CapabilityListItemResource doUpdate() {
    final CapabilityRequestResource request = new CapabilityRequestResource();
    request.setData(settingsAsResource());
    return client.put(id(), request).getData();
  }

  @Override
  protected void doRemove() {
    client.delete(id());
  }

  @Override
  public String type() {
    return settings().getTypeId();
  }

  @Override
  public String notes() {
    return settings().getNotes();
  }

  @Override
  public boolean isEnabled() {
    return settings().isEnabled();
  }

  @Override
  public boolean isActive() {
    return settings().isActive();
  }

  /**
   * @since 2.4
   */
  @Override
  public boolean hasErrors() {
    return settings().isError();
  }

  @Override
  public Map<String, String> properties() {
    final Map<String, String> propertiesMap = Maps.newHashMap();
    final List<CapabilityPropertyResource> properties = settings().getProperties();
    if (properties != null && !properties.isEmpty()) {
      for (final CapabilityPropertyResource property : properties) {
        propertiesMap.put(property.getKey(), property.getValue());
      }
    }
    return propertiesMap;
  }

  @Override
  public String property(final String key) {
    final CapabilityPropertyResource property = getProperty(checkNotNull(key));
    if (property != null) {
      return property.getValue();
    }
    return null;
  }

  @Override
  public boolean hasProperty(final String key) {
    return getProperty(key) != null;
  }

  @Override
  public String status() {
    return settings().getStatus();
  }

  /**
   * @since 2.4
   */
  @Override
  public String stateDescription() {
    return settings().getStateDescription();
  }

  @Override
  public C withNotes(final String notes) {
    settings().setNotes(notes);
    return me();
  }

  @Override
  public C enable() {
    return withEnabled(true).save();
  }

  @Override
  public C disable() {
    return withEnabled(false).save();
  }

  @Override
  public C withEnabled(final boolean enabled) {
    settings().setEnabled(enabled);
    return me();
  }

  @Override
  public C withProperty(final String key, final String value) {
    checkNotNull(key);
    getOrCreateProperty(key).setValue(value);
    return me();
  }

  @Override
  public C removeProperty(final String key) {
    settings().getProperties().remove(getProperty(key));
    return me();
  }

  private CapabilityResource settingsAsResource() {
    final CapabilityResource resource = new CapabilityResource();
    try {
      BeanUtils.copyProperties(resource, settings());
    }
    catch (final Exception e) {
      throw Throwables.propagate(e);
    }
    return resource;
  }

  private CapabilityPropertyResource getOrCreateProperty(final String key) {
    CapabilityPropertyResource property = getProperty(key);
    if (property == null) {
      property = new CapabilityPropertyResource().withKey(key);
      settings().getProperties().add(property);
    }
    return property;
  }

  private CapabilityPropertyResource getProperty(final String key) {
    final List<CapabilityPropertyResource> properties = settings().getProperties();
    if (properties != null && !properties.isEmpty()) {
      for (final CapabilityPropertyResource property : properties) {
        if (key.equals(property.getKey())) {
          return property;
        }
      }
    }
    return null;
  }

  private C me() {
    return (C) this;
  }

  private String xx;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CapabilityImpl)) {
      return false;
    }

    final CapabilityImpl that = (CapabilityImpl) o;

    if (id() != null ? !id().equals(that.id()) : that.id() != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return id() != null ? id().hashCode() : 0;
  }

}
