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
package org.sonatype.nexus.proxy.http;

import java.net.URL;

/**
 * This component resolves full URLs against known Nexus repositories.
 * 
 * @author cstamas
 */
public interface NexusURLResolver
{
    String ROLE = NexusURLResolver.class.getName();

    /**
     * Resolves the URL to a Nexus URL. The strategy how is it done and to what is it resolved is left to
     * implementation. The result -- if it is not null -- is a Nexus URL from where it is possible to get the artifact
     * addressed with the input URL.
     * 
     * @param url
     * @return the resolved Nexus URL or null if the URL is not resolvable by this resolver.
     */
    URL resolve( URL url );
}
