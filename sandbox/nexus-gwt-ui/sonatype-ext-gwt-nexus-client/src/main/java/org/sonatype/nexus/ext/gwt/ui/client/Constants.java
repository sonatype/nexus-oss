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
package org.sonatype.nexus.ext.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public interface Constants {

    String HOST = GWT.isScript()? Window.Location.getProtocol() + "//" + Window.Location.getHost()
                                : "http://localhost:8081";
    // FIXME: Find a way to determine the path automatically
    String PATH = "/nexus";

    String SERVICE = PATH + "/service/local";
    String SERVICE_REPOSITORIES = SERVICE + "/repositories";
    String SERVICE_REPOSITORY_STATUSES = SERVICE + "/repository_statuses";
    
    String STATUS = "status";
    String AUTHENTICATION = "authentication";
    String AUTHENTICATION_LOGIN = AUTHENTICATION + "/login";
    String AUTHENTICATION_LOGOUT = AUTHENTICATION + "/logout";

    String CONTENT = PATH + "/content";
    String CONTENT_REPOSITORIES = CONTENT + "/repositories";

}
