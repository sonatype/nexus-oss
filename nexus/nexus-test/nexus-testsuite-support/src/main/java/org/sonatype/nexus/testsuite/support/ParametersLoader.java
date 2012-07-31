/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.support;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.sisu.goodies.marshal.internal.jackson.JacksonMarshaller;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Utility class for loading Nexus integration tests parameters.
 *
 * @since 2.2
 */
public abstract class ParametersLoader
{

    private static final Logger LOGGER = LoggerFactory.getLogger( ParametersLoader.class );

    /**
     * Load test parameters from specified file. The file should contain a serialized Object[][] in json format.  Fails
     * if such a file cannot be found.
     *
     * @param parametersFile file containing parameters
     * @return test parameters
     */
    public static Collection<Object[]> loadTestParameters( final File parametersFile )
    {
        return loadTestParameters( parametersFile, true );
    }

    /**
     * Load test parameters from specified file. The file should contain a serialized Object[][] in json format.
     *
     * @param parametersFile file containing parameters
     * @param failIfNotFound whether it should fail if parameters file cannot be found or return an empty list
     * @return test parameters
     */
    public static Collection<Object[]> loadTestParameters( final File parametersFile, final boolean failIfNotFound )
    {
        if ( !checkNotNull( parametersFile ).exists() )
        {
            if ( failIfNotFound )
            {
                throw new RuntimeException( "Cannot find file '" + parametersFile.getAbsolutePath() + "'" );
            }
            LOGGER.info( "File '" + parametersFile.getAbsolutePath() + "' cannot be found and will be ignored" );
            return Lists.newArrayList();
        }
        LOGGER.info( "Loading test parameters from {}", parametersFile.getAbsolutePath() );
        try
        {
            final Object[][] parametersSets = new JacksonMarshaller().unmarshal(
                readFileToString( checkNotNull( parametersFile ) ), Object[][].class
            );
            if ( parametersSets == null )
            {
                return null;
            }
            return Arrays.asList( parametersSets );
        }
        catch ( final Exception e )
        {
            throw Throwables.propagate( e );
        }
    }

    /**
     * Load test specific parameters by looking up a file named "<test class name>-parameters.json" from classpath.
     * Fails if such a file cannot be found.
     *
     * @param testClass test class
     * @return test parameters, Never null.
     * @see {@link #loadTestParameters(java.io.File)}
     */
    public static Collection<Object[]> loadTestParameters( final Class testClass )
    {
        return loadTestParameters( testClass, true );
    }

    /**
     * Load test specific parameters by looking up a file named "<test class name>-parameters.json" from classpath.
     *
     * @param testClass      test class
     * @param failIfNotFound whether it should fail if parameters file cannot be found or return an empty list
     * @return test parameters, Never null.
     * @see {@link #loadTestParameters(java.io.File)}
     */
    public static Collection<Object[]> loadTestParameters( final Class testClass, final boolean failIfNotFound )
    {
        final String parametersFileName = checkNotNull( testClass ).getSimpleName() + "-parameters.json";
        final URL resource = testClass.getClassLoader().getResource( parametersFileName );
        if ( resource == null )
        {
            if ( failIfNotFound )
            {
                throw new RuntimeException( "Cannot find a file named '" + parametersFileName + "' in classpath" );
            }
            LOGGER.info( "File named '" + parametersFileName + "' cannot be found in classpath and will be ignored" );
            return Lists.newArrayList();
        }
        return loadTestParameters( new File( resource.getFile() ) );
    }

    /**
     * Load test parameters by looking up an "parameters.json" file in classpath. Fails if such a file cannot be found.
     *
     * @return test parameters
     * @see {@link #loadTestParameters(java.io.File)}
     */
    public static Collection<Object[]> loadDefaultTestParameters()
    {
        return loadDefaultTestParameters( true );
    }

    /**
     * Load test parameters by looking up an "parameters.json" file in classpath.
     *
     * @param failIfNotFound whether it should fail if parameters file cannot be found or return an empty list
     * @return test parameters
     * @see {@link #loadTestParameters(java.io.File)}
     */
    public static Collection<Object[]> loadDefaultTestParameters( final boolean failIfNotFound )
    {
        final URL resource = ParametersLoader.class.getClassLoader().getResource( "parameters.json" );
        if ( resource == null )
        {
            if ( failIfNotFound )
            {
                throw new RuntimeException( "Cannot find a file named 'parameters.json' in classpath" );
            }
            LOGGER.info( "File named 'parameters.json' cannot be found in classpath and will be ignored" );
            return Lists.newArrayList();
        }
        return loadTestParameters( new File( resource.getFile() ) );
    }

    /**
     * Load test parameters by looking up file specified via a system property named "NexusItSupport.parameters" (if
     * defined).  Fails if such a file cannot be found.
     *
     * @return test parameters
     * @see {@link #loadTestParameters(java.io.File)}
     */
    public static Collection<Object[]> loadSystemTestParameters()
    {
        return loadSystemTestParameters( true );
    }

    /**
     * Load test parameters by looking up file specified via a system property named "NexusItSupport.parameters" (if
     * defined).
     *
     * @param failIfNotFound whether it should fail if parameters file cannot be found or return an empty list
     * @return test parameters
     * @see {@link #loadTestParameters(java.io.File)}
     */
    public static Collection<Object[]> loadSystemTestParameters( final boolean failIfNotFound )
    {
        final String sysPropsParameters = System.getProperty( "NexusItSupport.parameters" );
        if ( sysPropsParameters == null )
        {
            return Lists.newArrayList();
        }
        return loadTestParameters( new File( sysPropsParameters ), failIfNotFound );
    }

    /**
     * Uses the first available (not null, not empty) set of parameters.
     *
     * @param parametersSets sets of parameters
     * @return first available parameters
     */
    public static Collection<Object[]> loadFirstAvailableTestParameters( final Collection<Object[]>... parametersSets )
    {
        for ( final Collection<Object[]> parametersSet : checkNotNull( parametersSets ) )
        {
            if ( parametersSet != null && !parametersSet.isEmpty() )
            {
                return parametersSet;
            }
        }
        throw new RuntimeException( "No parameters found" );
    }

    /**
     * Combines all parameters.
     *
     * @param parametersSets sets of parameters
     * @return combination of all parameters
     */
    public static Collection<Object[]> loadAllAvailableTestParameters( final Collection<Object[]>... parametersSets )
    {
        final Collection<Object[]> parameters = Lists.newArrayList();
        for ( final Collection<Object[]> parametersSet : checkNotNull( parametersSets ) )
        {
            if ( parametersSet != null && !parametersSet.isEmpty() )
            {
                parameters.addAll( parametersSet );
            }
        }
        if ( parameters.isEmpty() )
        {
            throw new RuntimeException( "No parameters found" );
        }
        return parameters;
    }

}
