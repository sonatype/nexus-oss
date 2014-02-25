/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.analytics.client

import groovy.transform.ToString
import org.sonatype.nexus.client.core.subsystem.SiestaClient
import org.sonatype.nexus.testsuite.analytics.client.Settings.SettingsXO

import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path

/**
 * Analytics Settings client.
 *
 * @since 2.8
 */
@Path("/service/siesta/analytics/settings")
interface Settings
extends SiestaClient
{

  @GET
  SettingsXO get()

  @PUT
  void set(SettingsXO settings);

  @ToString(includePackage = false, includeNames = true)
  static class SettingsXO
  {
    Boolean collection
    Boolean autosubmit
  }

}
