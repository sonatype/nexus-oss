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
package org.sonatype.nexus;

import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;

public class DefaultNexusTest
    extends AbstractNexusTestCase
{
    private DefaultNexus defaultNexus;

    private RepositoryTypeRegistry repositoryTypeRegistry;

    public DefaultNexus getDefaultNexus()
    {
        return defaultNexus;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (DefaultNexus) lookup( Nexus.class );

        repositoryTypeRegistry = lookup( RepositoryTypeRegistry.class );
    }

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public void testListRepositoryContentClasses()
        throws Exception
    {
        Map<String, ContentClass> plexusContentClasses = getContainer().lookupMap( ContentClass.class );

        Collection<ContentClass> contentClasses =  repositoryTypeRegistry.getContentClasses();

        assertEquals( plexusContentClasses.size(), contentClasses.size() );

        for ( ContentClass cc : plexusContentClasses.values() )
        {
            assertTrue( contentClasses.contains( cc ) );
        }
    }

    public void testBounceNexus()
        throws Exception
    {
        getDefaultNexus().stop();

        getDefaultNexus().start();
    }
}
