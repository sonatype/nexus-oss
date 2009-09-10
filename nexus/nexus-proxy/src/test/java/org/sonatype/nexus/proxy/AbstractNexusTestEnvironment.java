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

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.security.SecuritySystem;

public abstract class AbstractNexusTestEnvironment
    extends AbstractNexusTestCase
{
    /** The cache manager. */
    private CacheManager cacheManager;

    private Scheduler scheduler;

    private RepositoryItemUidFactory repositoryItemUidFactory;

    private MimeUtil mimeUtil;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( PLEXUS_HOME );

        PLEXUS_HOME.mkdirs();
        WORK_HOME.mkdirs();
        CONF_HOME.mkdirs();

        scheduler = lookup( Scheduler.class );

        cacheManager = lookup( CacheManager.class );

        repositoryItemUidFactory = lookup( RepositoryItemUidFactory.class );

        mimeUtil = lookup( MimeUtil.class );

        this.lookup( SecuritySystem.class ).setSecurityEnabled( false );
    }

    /**
     * Gets the cache manager.
     * 
     * @return the cache manager
     */
    protected CacheManager getCacheManager()
    {
        return cacheManager;
    }

    protected Scheduler getScheduler()
    {
        return scheduler;
    }

    protected RepositoryItemUidFactory getRepositoryItemUidFactory()
    {
        return repositoryItemUidFactory;
    }

    protected MimeUtil getMimeUtil()
    {
        return mimeUtil;
    }

}
