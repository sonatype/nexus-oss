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

package org.sonatype.nexus.logging.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.logging.LoggingPlugin;
import org.sonatype.nexus.logging.model.MarkerXO;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Log REST resource.
 *
 * @since 2.7
 */
@Named
@Singleton
@Path(LogResource.RESOURCE_URI)
public class LogResource
    extends ComponentSupport
    implements Resource
{

  public static final String RESOURCE_URI = LoggingPlugin.REST_PREFIX + "/log";

  private static final Logger log = LoggerFactory.getLogger(LogResource.class);

  @Inject
  public LogResource() {
  }

  /**
   * Logs a message at INFO level.
   *
   * @param marker message to be logger (cannot be null/empty)
   * @throws NullPointerException     If marker is null
   * @throws IllegalArgumentException If marker message is null or empty
   */
  @PUT
  @Path("/mark")
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(LoggingPlugin.PERMISSION_PREFIX + "update")
  public void put(final MarkerXO marker)
      throws Exception
  {
    checkNotNull(marker);
    checkArgument(StringUtils.isNotEmpty(marker.getMessage()));

    String asterixes = StringUtils.repeat("*", marker.getMessage().length() + 4);
    log.info("\n"
        + asterixes + "\n"
        + "* " + marker.getMessage() + " *" + "\n"
        + asterixes
    );
  }

}
