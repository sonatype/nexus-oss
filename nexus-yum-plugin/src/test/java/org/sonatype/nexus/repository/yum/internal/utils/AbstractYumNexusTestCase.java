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
package org.sonatype.nexus.repository.yum.internal.utils;

import static com.google.code.tempusfugit.temporal.Duration.millis;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.sonatype.nexus.repository.yum.internal.utils.RepositoryTestUtils.BASE_CACHE_DIR;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.sisu.litmus.testsupport.TestUtil;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.ThreadSleep;
import com.google.code.tempusfugit.temporal.Timeout;

public class AbstractYumNexusTestCase
    extends NexusTestSupport
{

    public static final TestUtil UTIL = new TestUtil( new Marker() );

    public static final File NEXUS_CONF_DIR = UTIL.resolveFile( "target/test-classes/nexus/sonatype-work/nexus/conf/" );

    public static final String TMP_DIR_KEY = "java.io.tmpdir";

    private String oldTmpDir;

    protected void waitFor( Condition condition )
        throws TimeoutException, InterruptedException
    {
        waitOrTimeout( condition, Timeout.timeout( seconds( 60 ) ), new ThreadSleep( millis( 30 ) ) );
    }

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        initConfigurations();
        super.setUp();
        copyTestConf();
        initRestApiSettings();
        injectFields();
    }

    private void copyTestConf()
    {
        try
        {
            copyDirectory( NEXUS_CONF_DIR, getConfHomeDir() );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Could not copy nexus configuration to a temp dir : " + NEXUS_CONF_DIR, e );
        }
    }

    private void injectFields()
        throws Exception, IllegalAccessException
    {
        for ( Field field : getAllFields() )
        {
            if ( field.getAnnotation( Inject.class ) != null )
            {
                lookupField( field, "" );
                continue;
            }

            Requirement requirement = field.getAnnotation( Requirement.class );
            if ( requirement != null )
            {
                lookupField( field, requirement.hint() );
            }
        }
    }

    private void lookupField( Field field, String hint )
        throws Exception, IllegalAccessException
    {
        Object value = lookup( field.getType(), hint );
        if ( !field.isAccessible() )
        {
            field.setAccessible( true );
            field.set( this, value );
            field.setAccessible( false );
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        System.setProperty( TMP_DIR_KEY, oldTmpDir );
        super.tearDown();
    }

    private void initConfigurations()
    {
        oldTmpDir = System.getProperty( TMP_DIR_KEY );
        System.setProperty( TMP_DIR_KEY, BASE_CACHE_DIR.getAbsolutePath() );
    }

    private void initRestApiSettings()
        throws Exception
    {
        NexusConfiguration config = lookup( NexusConfiguration.class );
        config.loadConfiguration( true );
    }

    private List<Field> getAllFields()
    {
        List<Field> fields = new ArrayList<Field>();
        Class<?> clazz = getClass();
        do
        {
            List<? extends Field> classFields = getFields( clazz );
            fields.addAll( classFields );
            clazz = clazz.getSuperclass();
        }
        while ( !Object.class.equals( clazz ) );
        return fields;
    }

    private List<? extends Field> getFields( Class<?> clazz )
    {
        return asList( clazz.getDeclaredFields() );
    }

    private static class Marker
    {

    }

}
