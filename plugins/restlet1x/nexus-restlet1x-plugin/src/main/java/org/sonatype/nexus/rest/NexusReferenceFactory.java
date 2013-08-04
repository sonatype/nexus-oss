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

import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.plexus.rest.DefaultReferenceFactory;
import org.sonatype.plexus.rest.ReferenceFactory;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Reference;
import org.restlet.data.Request;

@Component(role = ReferenceFactory.class)
public class NexusReferenceFactory
    extends DefaultReferenceFactory
{
  @Requirement
  private GlobalRestApiSettings globalRestApiSettings;

  @Override
  public Reference getContextRoot(Request request) {
    Reference result = null;

    if (globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
        && StringUtils.isNotEmpty(globalRestApiSettings.getBaseUrl())) {
      result = new Reference(globalRestApiSettings.getBaseUrl());
    }
    else {
      result = request.getRootRef();
    }

    // fix for when restlet is at webapp root
    if (StringUtils.isEmpty(result.getPath())) {
      result.setPath("/");
    }

    return result;
  }

}
