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
package org.sonatype.nexus.mime;

import java.io.File;
import java.util.Collection;

import org.junit.Test;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;

public class DefaultMimeUtilTest
    extends PlexusTestCaseSupport
{

    protected MimeUtil mimeUtil;

    protected MimeUtil2 medseaMimeUtil;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        mimeUtil = lookup( MimeUtil.class );

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

        testFile = getTestFile( "pom.xml" );
        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile.toURI().toURL() ) );

        testFile = getTestFile( "src/test/java/org/sonatype/nexus/mime/DefaultMimeUtilTest.java" );

        assertEquals( getMimeType( testFile ), mimeUtil.getMimeType( testFile ) );

        testFile = getTestFile( "target/test-classes/org/sonatype/nexus/mime/DefaultMimeUtilTest.class" );

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
