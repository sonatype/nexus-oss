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

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public class M2LayoutedM1ShadowRepositoryTest
    extends AbstractShadowRepositoryTest
{
    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M1TestsuiteEnvironmentBuilder( ss );
    }

    protected void addShadowReposes()
        throws ConfigurationException, IOException, ComponentLookupException
    {
        String masterId = "repo1-m1";

        M2LayoutedM1ShadowRepository shadow =
            (M2LayoutedM1ShadowRepository) getContainer().lookup( ShadowRepository.class, "m1-m2-shadow" );

        CRepository repoConf = new DefaultCRepository();

        repoConf.setProviderRole( ShadowRepository.class.getName() );
        repoConf.setProviderHint( "m1-m2-shadow" );
        repoConf.setId( masterId + "-m2" );
        repoConf.setIndexable( false );

        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        M1LayoutedM2ShadowRepositoryConfiguration exRepoConf = new M1LayoutedM2ShadowRepositoryConfiguration( exRepo );
        exRepoConf.setMasterRepositoryId( masterId );

        shadow.configure( repoConf );

        shadow.synchronizeWithMaster();

        getRepositoryRegistry().addRepository( shadow );

    }

    public void testProxyLastRequestedAttribute()
        throws Exception
    {
        addShadowReposes();

        testProxyLastRequestedAttribute( getRepositoryRegistry().getRepositoryWithFacet( "repo1-m1-m2",
            ShadowRepository.class ), "/activeio/activeio/2.1/activeio-2.1.pom", "/activeio/poms/activeio-2.1.pom" );
    }
}