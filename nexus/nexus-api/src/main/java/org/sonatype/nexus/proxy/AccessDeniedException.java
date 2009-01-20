/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy;

/**
 * Thrown when a request is denied by Nexus for security reasons. This exception should be ALWAYS considered as
 * "authorization denied" type of stuff, since Nexus does not deal with authentication. Simply taken, this exception is
 * thrown for lack of permissions of the already authenticated subject.
 * 
 * @author cstamas
 */
public class AccessDeniedException
    extends AuthorizationException
{
    private static final long serialVersionUID = 8341250956517740603L;

    private final ResourceStoreRequest request;

    public AccessDeniedException( ResourceStoreRequest request, String msg )
    {
        super( msg );

        this.request = request;
    }

    /**
     * The RepositoryItemUid that is forbidden to access.
     * 
     * @return
     */
    public ResourceStoreRequest getResourceStoreRequest()
    {
        return this.request;
    }
}
