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

import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.DefaultRepository;

/**
 * The default Eclipse Update Site repository. This class does a) proxying and caching requested jars and b)
 * modifies/normalizes the sitemap (site.xml) file in the following manner (while storing [somehow, even under alternate
 * name] the original too): it should force Eclipse Update Manager to always request stuff from Nexus, hence it should
 * remove all url,digestUrl,mirrors and other stuff from emitted site.xml, but it should know from where to get those if
 * requested!
 * 
 * @see http://help.eclipse.org/help33/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/misc/update_sitemap.html
 * @author cstamas
 * plexus.component instantiation-strategy="per-lookup" role-hint="eclipse-update-site"
 */
public class EclipseUpdateSiteRepository
    extends DefaultRepository
{
    public ContentClass getRepositoryContentClass()
    {
        return new EclipseUpdateSiteContentClass();
    }
}
