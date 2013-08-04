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

package org.sonatype.nexus.plugins.ui;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.NexusApplicationCustomizer;
import org.sonatype.plexus.rest.RetargetableRestlet;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Installs a filter before the NexusApplication root that will redirect /images to /static/images.
 *
 * @since 2.5
 */
@Named("ui-extjs3-redirector")
@Singleton
public class RedirectingNexusApplicationCustomizer
    extends AbstractLogEnabled
    implements NexusApplicationCustomizer
{
  @Override
  public void customize(final NexusApplication app, final RetargetableRestlet root) {
    app.setRoot(new Redirector(app));
  }

  private class Redirector
      extends RetargetableRestlet
  {


    public Redirector(final NexusApplication app) {
      super(app.getContext());
      setNext(app.getRoot());
    }

    @Override
    protected int doHandle(final Request request, final Response response) {
      int state = super.doHandle(request, response);

      if (response.getStatus().getCode() != 404) {
        return state;
      }

      final String path = request.getResourceRef().getPath();
      final String ctxPath = request.getResourceRef().getBaseRef().getPath();

      if (path.startsWith(ctxPath + "images/")) {
        response.redirectPermanent(path.replace(ctxPath + "images/", ctxPath + "static/images/"));
        return STOP;
      }

      return state;
    }
  }
}
