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
package org.sonatype.nexus.proxy.maven.site;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractWebSiteRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;

/**
 * The default Maven Site Repository.
 * 
 * @author cstamas
 */
@Component( role = WebSiteRepository.class, hint = "maven-site", instantiationStrategy = "per-lookup", description = "Maven Site Repository" )
public class DefaultMavenSiteRepository
    extends AbstractWebSiteRepository
    implements MavenSiteRepository
{
    @Requirement( hint = "maven-site" )
    private ContentClass contentClass;

    @Requirement
    private DefaultMavenSiteRepositoryConfigurator repositoryConfigurator;

    private RepositoryKind repositoryKind;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind = new DefaultRepositoryKind( MavenSiteRepository.class, null );
        }

        return repositoryKind;
    }

    public void deploySiteBundle( String prefix, InputStream bundle )
        throws IOException
    {
        throw new UnsupportedOperationException( "Deploy of the bundle is not yet implemented!" );
    }

    @Override
    public Configurator getConfigurator()
    {
        return repositoryConfigurator;
    }

}
