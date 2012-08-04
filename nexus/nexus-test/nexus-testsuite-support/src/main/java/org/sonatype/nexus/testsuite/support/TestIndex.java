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

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Boolean.TRUE;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static javax.xml.bind.Marshaller.JAXB_FRAGMENT;
import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.apache.commons.io.FileUtils.writeStringToFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.sonatype.nexus.testsuite.support.index.IndexXO;
import org.sonatype.nexus.testsuite.support.index.TestInfoXO;
import org.sonatype.nexus.testsuite.support.index.TestXO;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * @since 1.4
 */
public class TestIndex
    extends TestWatcher
{

    private final File indexDir;

    private Description description;

    private boolean initialized;

    private File testDir;

    private File indexXml;

    private IndexXO index;

    private TestXO test;

    private long startTime;

    public TestIndex( final File indexDir )
    {
        this.indexDir = indexDir;
    }

    @Override
    protected void starting( final Description description )
    {
        this.description = Preconditions.checkNotNull( description );
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void succeeded( final Description description )
    {
        initialize();
        test.setSuccess( true );
    }

    @Override
    protected void failed( final Throwable e, final Description description )
    {
        initialize();
        test.setSuccess( false );
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter( sw );
        e.printStackTrace( pw );
        test.setThrowableMessage( e.getMessage() );
        test.setThrowableStacktrace( sw.toString() );
    }

    @Override
    protected void finished( final Description description )
    {
        initialize();
        test.setDuration( ( System.currentTimeMillis() - startTime ) / 1000 );
        save();
    }

    public File getDirectory()
    {
        initialize();
        return testDir;
    }

    public File getDirectory( final String name )
    {
        final File dir = new File( getDirectory(), name );
        checkState(
            ( dir.mkdirs() || dir.exists() ) && dir.isDirectory(),
            "Not able to create test directory '{}'",
            dir.getAbsolutePath()
        );
        return dir;
    }

    public void recordInfo( final String key, final String value )
    {
        initialize();
        test.withTestInfos( new TestInfoXO().withLink( false ).withKey( key ).withValue( value ) );
    }

    public void recordLink( final String key, final String value )
    {
        initialize();
        test.withTestInfos( new TestInfoXO().withLink( true ).withKey( key ).withValue( value ) );
    }

    public void recordLink( final String key, final File file )
    {
        if ( file.exists() )
        {
            initialize();
            try
            {
                // TODO use a better algorithm to relativize
                final String canonicalFile = file.getCanonicalPath();
                final String canonicalDir = indexDir.getCanonicalPath();
                recordLink( key, canonicalFile.substring( canonicalDir.length() + 1 ) );
            }
            catch ( IOException e )
            {
                // TODO?
            }
        }
    }

    private void initialize()
    {
        checkState( description != null );
        if ( !initialized )
        {
            load();

            index.setCounter( index.getCounter() + 1 );

            test = new TestXO()
                .withIndex( index.getCounter() )
                .withClassName( description.getClassName() )
                .withMethodName( description.getMethodName() );

            index.withTests( test );

            save();
            copyStyleSheets();

            testDir = new File( indexDir, String.valueOf( index.getCounter() ) );
            checkState(
                ( testDir.mkdirs() || testDir.exists() ) && testDir.isDirectory(),
                "Not able to create test directory '{}'",
                testDir.getAbsolutePath()
            );
            initialized = true;
        }
    }

    private void copyStyleSheets()
    {
        try
        {
            copyURLToFile(
                getClass().getClassLoader().getResource( "index.css" ),
                new File( indexDir, "index.css" )
            );
            copyURLToFile(
                getClass().getClassLoader().getResource( "index.xsl" ),
                new File( indexDir, "index.xsl" )
            );
        }
        catch ( IOException e )
        {
            // well, that's it!
        }
    }

    private void load()
    {
        indexXml = new File( indexDir, "index.xml" );
        index = new IndexXO().withCounter( 0 );
        if ( indexXml.exists() )
        {
            try
            {
                final Unmarshaller unmarshaller = JAXBContext.newInstance( IndexXO.class ).createUnmarshaller();
                index = (IndexXO) unmarshaller.unmarshal( indexXml );
            }
            catch ( Exception e )
            {
                // TODO Should we fail the test if we cannot write the index?
                throw Throwables.propagate( e );
            }
        }
    }

    private void save()
    {
        try
        {
            final StringWriter writer = new StringWriter();
            final PrintWriter printWriter = new PrintWriter( writer );

            printWriter.println( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" );
            printWriter.println( "<?xml-stylesheet type=\"text/css\" href=\"index.css\"?>" );
            printWriter.println( "<?xml-stylesheet type=\"text/xsl\" href=\"index.xsl\"?>" );

            final Marshaller marshaller = JAXBContext.newInstance( IndexXO.class ).createMarshaller();
            marshaller.setProperty( JAXB_FORMATTED_OUTPUT, TRUE );
            marshaller.setProperty( JAXB_FRAGMENT, TRUE );
            marshaller.marshal( index, writer );

            writeStringToFile( indexXml, writer.toString() );
        }
        catch ( Exception e )
        {
            // TODO Should we fail the test if we cannot write the index?
            throw Throwables.propagate( e );
        }
    }

}