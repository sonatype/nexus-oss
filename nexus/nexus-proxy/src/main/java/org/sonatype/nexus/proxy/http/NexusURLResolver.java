/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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
