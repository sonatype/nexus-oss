/**
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

package org.sonatype.nexus.analytics.rest

import groovy.transform.ToString
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.analytics.internal.AutoSubmitCapabilityDescriptor
import org.sonatype.nexus.analytics.internal.CollectionCapabilityDescriptor
import org.sonatype.nexus.plugins.capabilities.CapabilityReference
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry
import org.sonatype.nexus.plugins.capabilities.CapabilityType
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.siesta.common.Resource

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Analytics settings resource.
 *
 * @since 2.8
 */
@Named
@Singleton
@Path(SettingsResource.RESOURCE_URI)
class SettingsResource
    extends ComponentSupport
    implements Resource
{
  static final String RESOURCE_URI = '/analytics/settings'

  @Inject
  CapabilityRegistry capabilities

  private CapabilityReference capabilityReference(CapabilityType type) {
    def refs = capabilities.get(CapabilityReferenceFilterBuilder.capabilities().withType(type))
    assert !refs.isEmpty(), "Required capability has been removed; type: $type"
    return refs.iterator().next()
  }

  private void toggleCapabilityEnabled(CapabilityType type, boolean enable) {
    def ref = capabilityReference(type)
    if (enable) {
      capabilities.enable(ref.context().id());
    }
    else {
      capabilities.disable(ref.context().id());
    }
  }

  @ToString(includePackage = false, includeNames = true)
  static class SettingsXO
  {
    // NOTE: Using java.lang.Boolean here to prevent Jackson issues marshalling due to Groovy generated getters :-(

    Boolean collection = false

    Boolean autosubmit = false
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RequiresPermissions('nexus:analytics')
  SettingsXO get() {
    return new SettingsXO(
        collection: capabilityReference(CollectionCapabilityDescriptor.TYPE).context().enabled,
        autosubmit: capabilityReference(AutoSubmitCapabilityDescriptor.TYPE).context().enabled
    )
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RequiresPermissions('nexus:analytics')
  void put(SettingsXO settings) {
    log.info("Update settings: $settings")

    toggleCapabilityEnabled(CollectionCapabilityDescriptor.TYPE, settings.collection)
    toggleCapabilityEnabled(AutoSubmitCapabilityDescriptor.TYPE, settings.autosubmit)
  }
}