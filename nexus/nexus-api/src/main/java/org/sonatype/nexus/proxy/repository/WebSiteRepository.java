/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.plugin.ExtensionPoint;

/**
 * A hosted repository that serves up "web" content (static HTML files). Default behaviour: If a request results in
 * collection, it will look in that collection for any existing welcome file and serve that up instead of collection. If
 * no welcome file found, it falls back to collection/index view.
 * 
 * @author cstamas
 */
@ExtensionPoint
public interface WebSiteRepository
    extends HostedRepository
{
    /**
     * Key to be used in a repository request to signal if we should use the welcome files (index.html, index.htm, etc)
     * or if we should return the collection.
     */
    public static final String USE_WELCOME_FILES_KEY = "useWelcomeFiles";

    /**
     * Gets the list of unmodifiable "welcome" file names. Example: "index.html", "index.htm".
     * 
     * @return
     */
    List<String> getWelcomeFiles();

    /**
     * Sets the list of welcome files.
     * 
     * @param files
     */
    void setWelcomeFiles( List<String> files );
}
