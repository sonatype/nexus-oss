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

package org.sonatype.nexus.atlas.rest

import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.security.SecuritySystem
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.siesta.common.Resource

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Create support ZIP files.
 *
 * @since 2.7
 */
@Named
@Singleton
@Path(SupportZipResource.RESOURCE_URI)
class SupportZipResource
extends ComponentSupport
implements Resource
{
  static final String RESOURCE_URI = '/atlas/support-zip'

  /**
   * Create a support ZIP file.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RequiresPermissions('nexus:atlas')
  Map userDiagnostic(final Map request) {
    log.info 'Creating support ZIP for request: {}', request

    // TODO: Generate zip file, return reference to the file create for download

    return [
        fileName: 'somefile-12345.zip'
    ]
  }
}