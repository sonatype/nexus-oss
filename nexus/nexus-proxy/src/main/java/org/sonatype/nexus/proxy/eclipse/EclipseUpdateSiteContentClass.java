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
package org.sonatype.nexus.proxy.eclipse;

import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;

/**
 * The Eclipse Update Site content class.
 * 
 * @author cstamas
 * plexus.component role-hint="eclipse-update-site"
 */
public class EclipseUpdateSiteContentClass
    extends AbstractIdContentClass
{
    private static final String ID = "eclipse-update-site";

    public String getId()
    {
        return ID;
    }
}
