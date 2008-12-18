/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.router;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component( role = RepositoryRouter.class, hint = "groups-m2" )
public class TestM2GroupIdBasedRouter
    extends GroupIdBasedRepositoryRouter
{

    public static final String ID = "groups-m2";

    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }

    public String getId()
    {
        return ID;
    }

}
