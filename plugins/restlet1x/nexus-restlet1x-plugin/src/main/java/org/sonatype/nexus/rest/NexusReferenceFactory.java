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

package org.sonatype.nexus.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.plexus.rest.DefaultReferenceFactory;

import org.apache.commons.lang.StringUtils;
import org.restlet.data.Reference;
import org.restlet.data.Request;

@Named
@Singleton
public class NexusReferenceFactory
    extends DefaultReferenceFactory
{
  private final GlobalRestApiSettings globalRestApiSettings;

  @Inject
  public NexusReferenceFactory(final GlobalRestApiSettings globalRestApiSettings) {
    this.globalRestApiSettings = globalRestApiSettings;
  }

  @Override
  public Reference getContextRoot(Request request) {
    Reference result = null;

    if (globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
        && StringUtils.isNotEmpty(globalRestApiSettings.getBaseUrl())) {
      result = new Reference(globalRestApiSettings.getBaseUrl());
    }
    else {
      // TODO: NEXUS-6045 hack, Restlet app root is now "/service/local", so going up 2 levels!
      result = request.getRootRef().getParentRef().getParentRef();
    }

    // fix for when restlet is at webapp root
    if (StringUtils.isEmpty(result.getPath())) {
      result.setPath("/");
    }

    return result;
  }

}
