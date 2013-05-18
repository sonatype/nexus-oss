/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mime;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link DefaultMimeUtil}.
 */
public class DefaultMimeUtilTest
    extends TestSupport
{
    protected MimeUtil mimeUtil;

    protected MimeUtil2 medseaMimeUtil;

    @Before
    public void setUp() throws Exception {
        mimeUtil = new DefaultMimeUtil();
        medseaMimeUtil = new MimeUtil2();
        medseaMimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );
    }

    @SuppressWarnings( "unchecked" )
    protected String getMimeType( File file )
    {
        Collection<MimeType> mimeTypes = medseaMimeUtil.getMimeTypes( file );

        return MimeUtil2.getMostSpecificMimeType( mimeTypes ).toString();
    }

    protected String getMimeType( String fileName )
    {
        Collection<MimeType> mimeTypes = medseaMimeUtil.getMimeTypes( fileName );

        return MimeUtil2.getMostSpecificMimeType( mimeTypes ).toString();
    }

    @Test
    public void testSimple()
        throws Exception
    {
        File testFile = null;

        testFile = util.resolveFile( "pom.xml" );
        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile.toURI().toURL() ) );

        testFile = util.resolveFile( "src/test/java/org/sonatype/nexus/mime/DefaultMimeUtilTest.java" );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );

        testFile = util.resolveFile( "target/test-classes/org/sonatype/nexus/mime/DefaultMimeUtilTest.class" );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );
    }

    @Test
    public void testSimpleByName()
        throws Exception
    {
        String testFileName = null;

        testFileName = "pom.xml";

        assertEquals( getMimeType( testFileName ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "/some/path/pom.xml";

        assertEquals( getMimeType( "pom.xml" ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "\\some\\path\\pom.xml";

        assertEquals( getMimeType( "pom.xml" ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "DefaultMimeUtilTest.java";

        assertEquals( getMimeType( testFileName ), mimeUtil.getMimeType( testFileName ) );

        testFileName = "DefaultMimeUtilTest.class";

        assertEquals( getMimeType( testFileName ), mimeUtil.getMimeType( testFileName ) );
    }
}
