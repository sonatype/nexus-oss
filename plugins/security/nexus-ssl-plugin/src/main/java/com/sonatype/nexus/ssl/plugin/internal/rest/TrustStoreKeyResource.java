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
package com.sonatype.nexus.ssl.plugin.internal.rest;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sonatype.nexus.ssl.model.TrustStoreKeyXO;
import com.sonatype.nexus.ssl.plugin.SSLPlugin;
import com.sonatype.nexus.ssl.plugin.spi.CapabilityManager;

import org.sonatype.nexus.capability.CapabilityReference;
import org.sonatype.siesta.Resource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Trust Store key resource.
 *
 * @since ssl 1.0
 */
@Named
@Singleton
@Path(SSLPlugin.REST_PREFIX + "/truststore/key")
public class TrustStoreKeyResource
    extends ComponentSupport
    implements Resource
{

  private final ReadWriteLock lock;

  private final Map<String, CapabilityManager> managers;

  @Inject
  public TrustStoreKeyResource(final Map<String, CapabilityManager> managers) {
    this.managers = checkNotNull(managers);
    lock = new ReentrantReadWriteLock();
  }

  @GET
  @Path("/{type}/{id}")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:read")
  public TrustStoreKeyXO get(final @PathParam("type") String type,
                             final @PathParam("id") String id)
      throws Exception
  {
    try {
      lock.readLock().lock();
      final CapabilityReference reference = getManager(type).get(id);
      return new TrustStoreKeyXO().withEnabled(
          reference != null && reference.context().isEnabled()
      );
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @PUT
  @Path("/{type}/{id}")
  @Consumes({APPLICATION_XML, APPLICATION_JSON})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "trustedkeys:update")
  public TrustStoreKeyXO update(final @PathParam("type") String type,
                                final @PathParam("id") String id,
                                final TrustStoreKeyXO key)
      throws Exception
  {
    try {
      lock.writeLock().lock();
      getManager(type).enable(id, key.isEnabled());
    }
    finally {
      lock.writeLock().unlock();
    }
    return get(type, id);
  }

  private CapabilityManager getManager(final String type) {
    final CapabilityManager manager = managers.get(type);
    if (manager == null) {
      throw new NotFoundException("Capability manager of type '" + type + "' are not supported");
    }
    return manager;
  }

}
