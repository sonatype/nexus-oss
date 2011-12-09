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
package org.sonatype.nexus;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.junit.Assert;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.nexus.binders.NexusAnnotatedBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;

/**
 * Base class to be extended by Nexus plugins tests. Beside the standard {@link AbstractNexusTestCase} functionality
 * will scan additional paths for components, such as "target/classes", "target/test-classes", or ant-like classpath
 * entries.
 * 
 * @author ...
 * @author Alin Dreghiciu
 */
public abstract class AbstractPluginTestCase
    extends AbstractNexusTestCase
{
    protected String[] sourceDirectories = { "target/classes", "target/test-classes" };

    protected void setupContainer()
    {
        super.setupContainer();

        try
        {
            final List<URL> scanList = new ArrayList<URL>();

            final String[] sourceDirs = getSourceDirectories();
            for ( String sourceDir : sourceDirs )
            {
                scanList.add( getTestFile( sourceDir ).toURI().toURL() );
            }

            final List<String> exportedClassNames = new ArrayList<String>();
            final List<RepositoryTypeDescriptor> repositoryTypes = new ArrayList<RepositoryTypeDescriptor>();

            final ClassSpace annSpace =
                new URLClassSpace( getContainer().getContainerRealm(), scanList.toArray( new URL[scanList.size()] ) );
            final NexusAnnotatedBeanModule nexusPluginModule =
                new NexusAnnotatedBeanModule( annSpace, new HashMap<String, String>(), exportedClassNames,
                    repositoryTypes );
            final List<PlexusBeanModule> modules = Arrays.<PlexusBeanModule> asList( nexusPluginModule );

            // register new injector
            ( (DefaultPlexusContainer) getContainer() ).addPlexusInjector( modules );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            Assert.fail( "Failed to create plexus container: " + e.getMessage() );
        }
    }

    /**
     * Returns a list of source directories to be scanned for components. The list is composed from
     * {@link #getDefaultSourceDirectories()}, {@link #getAdditionalSourceDirectories()} and the dependent plugins
     * directories.
     * 
     * @return list of source directories (should not be null)
     */
    protected String[] getSourceDirectories()
    {
        final List<String> directories = new ArrayList<String>();
        final String[] defaultDirs = getDefaultSourceDirectories();
        if ( defaultDirs != null && defaultDirs.length > 0 )
        {
            directories.addAll( Arrays.asList( defaultDirs ) );
        }
        final String[] additionalDirs = getAdditionalSourceDirectories();
        if ( additionalDirs != null && additionalDirs.length > 0 )
        {
            directories.addAll( Arrays.asList( additionalDirs ) );
        }

        return directories.toArray( new String[directories.size()] );
    }

    /**
     * Returns a list of default directories to be scanned for components.
     * 
     * @return list of source directories (should not be null)
     */
    protected String[] getDefaultSourceDirectories()
    {
        return sourceDirectories;
    }

    /**
     * Returns a list of additional directories to be scanned for components beside default ones. By default the list is
     * empty but can be overridden by tests in order to add additional directories.
     * 
     * @return list of source directories (should not be null)
     */
    protected String[] getAdditionalSourceDirectories()
    {
        return new String[0];
    }

    /**
     * Returns a list of claspath entry paths to be scanned.
     * 
     * @return list of classpath entry paths (should not be null)
     */
    protected String[] getClasspathEntries()
    {
        return new String[0];
    }

}
