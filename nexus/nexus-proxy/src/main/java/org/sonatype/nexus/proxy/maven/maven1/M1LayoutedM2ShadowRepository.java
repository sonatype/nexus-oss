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
package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.Validator;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * A shadow repository that transforms M2 layout of master to M1 layouted shadow.
 * 
 * @author cstamas
 */
@Component( role = ShadowRepository.class, hint = "m2-m1-shadow", instantiationStrategy = "per-lookup", description = "Maven2 to Maven1" )
public class M1LayoutedM2ShadowRepository
    extends LayoutConverterShadowRepository
{
    @Requirement( hint = "maven1" )
    private ContentClass contentClass;

    @Requirement( hint = "maven2" )
    private ContentClass masterContentClass;

    @Requirement
    private M1LayoutedM2ShadowRepositoryConfigurator m1LayoutedM2ShadowRepositoryConfigurator;

    @Requirement
    private M1LayoutedM2ShadowRepositoryValidator m1LayoutedM2ShadowRepositoryValidator;

    @Override
    public M1LayoutedM2ShadowRepositoryConfiguration getExternalConfiguration()
    {
        return (M1LayoutedM2ShadowRepositoryConfiguration) super.getExternalConfiguration();
    }

    public GavCalculator getGavCalculator()
    {
        return getM1GavCalculator();
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return m1LayoutedM2ShadowRepositoryConfigurator;
    }

    @Override
    protected Validator getValidator()
    {
        return m1LayoutedM2ShadowRepositoryValidator;
    }

    protected String transformMaster2Shadow( String path )
        throws IllegalArtifactCoordinateException
    {
        return transformM2toM1( path );
    }

    protected String transformShadow2Master( String path )
        throws IllegalArtifactCoordinateException
    {
        return transformM1toM2( path );
    }
}
