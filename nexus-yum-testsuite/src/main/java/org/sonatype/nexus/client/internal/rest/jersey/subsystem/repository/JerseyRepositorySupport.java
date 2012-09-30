/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;
import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;

import com.google.common.base.Throwables;

@Named
public abstract class JerseyRepositorySupport<T extends Repository<?, ?>, S extends RepositoryBaseResource> extends
    SubsystemSupport<JerseyNexusClient> implements Repository<T, S> {

  private final S settings;

  private boolean shouldCreate;

  @Inject
  public JerseyRepositorySupport(final JerseyNexusClient nexusClient) {
    super(nexusClient);
    settings = checkNotNull(createSettings());
    shouldCreate = true;
    refresh();
  }

  @Override
  public S settings() {
    return settings;
  }

  @Override
  public synchronized T refresh() {
    overwriteSettingsWith(getSettings());
    return me();
  }

  @Override
  public synchronized T save() {
    overwriteSettingsWith(saveSettings());
    shouldCreate = false;
    saveSettings();
    return me();
  }

  @Override
  public synchronized T remove() {
    doRemove();
    shouldCreate = true;
    return me();
  }

  protected abstract S createSettings();

  protected abstract Class<? extends NexusResponse> getResponseClass();

  protected abstract S getData(Object response);

  String uri() {
    return "repositories";
  }

  void overwriteSettingsWith(final S source) {
    try {
      BeanUtils.copyProperties(settings(), source == null ? checkNotNull(createSettings()) : source);
    } catch (final Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private T me() {
    return (T) this;
  }

  private S getSettings() {
    if (settings().getId() == null) {
      return null;
    }

    final Object response = getNexusClient().serviceResource(uri() + "/" + settings().getId()).get(getResponseClass());

    if (response == null) {
      return null;
    }

    shouldCreate = false;

    return getData(response);
  }

  private S saveSettings() {
    final RepositoryResourceResponse request = new RepositoryResourceResponse();
    request.setData(settings);

    final Object response;
    if (shouldCreate) {
      response = getNexusClient().serviceResource(uri()).post(getResponseClass(), request);
    } else {
      response = getNexusClient().serviceResource(uri() + "/" + settings().getId()).put(getResponseClass(), request);
    }

    return getData(response);
  }

  private void doRemove() {
    getNexusClient().serviceResource(uri() + "/" + settings().getId()).delete();
  }

}
