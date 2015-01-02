/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.wonderland.rest

import org.apache.shiro.subject.Subject
import org.sonatype.security.SecuritySystem
import org.sonatype.siesta.Resource
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.core.Context

/**
 * Session resource.
 *
 * @since 3.0
 */
@Named
@Singleton
@Path(SessionResource.RESOURCE_URI)
class SessionResource
extends ComponentSupport
implements Resource
{
  static final String RESOURCE_URI = '/wonderland/session'

  @DELETE
  void reset(@Context HttpServletRequest request) {
    HttpSession session = request.getSession(false)
    if (session) {
      session.invalidate()
    }
  }

}
