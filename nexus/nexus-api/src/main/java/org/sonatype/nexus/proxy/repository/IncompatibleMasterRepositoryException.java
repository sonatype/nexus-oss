/**
 * Sonatype NexusTM [Open Source Version].
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
package org.sonatype.nexus.proxy.repository;

/**
 * Throws when an incompatible master is assigned to a shadow repository.
 * 
 * @author cstamas
 */
public class IncompatibleMasterRepositoryException
    extends Exception
{
    private static final long serialVersionUID = -5676236705854300582L;

    private final ShadowRepository shadow;

    private final Repository master;

    public IncompatibleMasterRepositoryException( ShadowRepository shadow, Repository master )
    {
        this( "Master repository ID='" + master.getId() + "' is incompatible with shadow repository ID='"
            + shadow.getId() + "' because of it's ContentClass", shadow, master );
    }

    public IncompatibleMasterRepositoryException( String message, ShadowRepository shadow, Repository master )
    {
        super( message );

        this.shadow = shadow;

        this.master = master;
    }

    public ShadowRepository getShadow()
    {
        return shadow;
    }

    public Repository getMaster()
    {
        return master;
    }
}
