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
package org.sonatype.nexus.proxy.maven.maven2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryShadowArtifactVersionConstraint;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepositoryConfigurator;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

@Component( role = ShadowRepositoryConfigurator.class, hint = "m2-constrained" )
public class ConstrainedM2ShadowRepositoryConfigurator
    extends AbstractShadowRepositoryConfigurator
    implements ShadowRepositoryConfigurator
{
    public ShadowRepository updateRepositoryFromModel( ShadowRepository old, ApplicationConfiguration configuration,
        CRepositoryShadow repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, Repository masterRepository )
        throws InvalidConfigurationException
    {
        ShadowRepository result = super.updateRepositoryFromModel( old, configuration, repo, rsc, ls, masterRepository );

        if ( repo.getArtifactVersionConstraints() != null )
        {
            Map<String, String> versionMap = new HashMap<String, String>( repo.getArtifactVersionConstraints().size() );

            for ( CRepositoryShadowArtifactVersionConstraint constraint : (List<CRepositoryShadowArtifactVersionConstraint>) repo
                .getArtifactVersionConstraints() )
            {
                versionMap.put( constraint.getGroupId() + ":" + constraint.getArtifactId(), constraint.getVersion() );
            }

            ( (ConstrainedM2ShadowRepository) result ).setVersionMap( versionMap );
        }

        return result;
    }

}
