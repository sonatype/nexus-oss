/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The event fired on Expiring Proxy Caches (proxy repository's local storage is actually proxy-cache). This event is
 * fired only when {@link Repository#expireCaches(org.sonatype.nexus.proxy.ResourceStoreRequest)} method is invoked on a
 * {@link ProxyRepository} repository that has proxy facet available (is proxy).
 * 
 * @author cstamas
 * @since 2.0
 */
public class RepositoryEventExpireProxyCaches
    extends RepositoryMaintenanceEvent
{
    /** From where it happened */
    private final String path;

    public RepositoryEventExpireProxyCaches( final ProxyRepository repository, final String path )
    {
        super( repository );
        this.path = path;
    }

    @Override
    public ProxyRepository getRepository()
    {
        return (ProxyRepository) super.getRepository();
    }

    public String getPath()
    {
        return path;
    }
}
