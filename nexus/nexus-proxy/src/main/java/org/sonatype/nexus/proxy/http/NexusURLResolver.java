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
