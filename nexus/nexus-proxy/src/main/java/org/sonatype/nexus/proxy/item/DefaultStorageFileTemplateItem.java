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
package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageFileTemplateItem. This is a specialization of file item: it's "body" serves as Template, and
 * will be interpolated against context, before served.
 */
public class DefaultStorageFileTemplateItem
    extends DefaultStorageFileItem
{
    /**
     * Instantiates a new default storage file item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param contentLocator the content locator
     */
    public DefaultStorageFileTemplateItem( Repository repository, ResourceStoreRequest request, boolean canRead,
                                           boolean canWrite, ContentLocator contentLocator )
    {
        super( repository, request, canRead, canWrite, contentLocator );

        getItemContext().put( ContentGenerator.CONTENT_GENERATOR_ID, VelocityContentGenerator.VELOCITY );
    }

    /**
     * Instantiates a new default storage file item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param template the velocity template to be used
     */
    public DefaultStorageFileTemplateItem( Repository repository, ResourceStoreRequest request, boolean canRead,
                                           boolean canWrite, String template )
    {
        this( repository, request, canRead, canWrite, new StringContentLocator( template ) );
    }

    /**
     * Instantiates a new default storage file item.
     * 
     * @param RepositoryRouter router
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param contentLocator the content locator
     */
    public DefaultStorageFileTemplateItem( RepositoryRouter router, ResourceStoreRequest request, boolean canRead,
                                           boolean canWrite, ContentLocator contentLocator )
    {
        super( router, request, canRead, canWrite, contentLocator );

        getItemContext().put( ContentGenerator.CONTENT_GENERATOR_ID, VelocityContentGenerator.VELOCITY );
    }
}
