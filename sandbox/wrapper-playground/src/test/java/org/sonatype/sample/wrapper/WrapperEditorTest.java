/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;

public class WrapperEditorTest
{
    protected WrapperEditor prepareCase( String caseNum )
        throws IOException
    {
        File source = new File( "target/test-classes/c01/wrapper.conf" );

        FileUtils.copyFile( new File( "src/test/resources/c01/wrapper.conf" ), source );

        return WrapperHelper.getWrapperEditor( source );
    }

    protected void validateCase( String caseNum )
        throws IOException
    {
        File source = new File( "target/test-classes/" + caseNum + "/wrapper.conf" );

        File expectedResult = new File( "target/test-classes/" + caseNum + "/wrapper.conf.result" );

        assertEquals( "wrapper.conf does not match!", FileUtils.loadFile( expectedResult ), FileUtils.loadFile( source ) );
    }

    @Test
    public void testCase01()
        throws Exception
    {
        WrapperEditor editor = prepareCase( "c01" );

        assertEquals( "The startup timeout does not match!", 90, editor.getWrapperStartupTimeout() );

        Map<String, String> allKeyValuePairs = editor.getWrapperConfWrapper().getAllKeyValuePairs();

        assertEquals( "Not all entries are read!", 20, allKeyValuePairs.size() );

        editor.setWrapperStartupTimeout( 130 );

        String mainClass = editor.getWrapperJavaMainclass();

        assertEquals( "Main class does not match!", "org.sonatype.sample.wrapper.ApplicationA", mainClass );

        editor.setWrapperJavaMainclass( mainClass.substring( 0, mainClass.length() - 1 ) + "B" );

        editor.save();

        validateCase( "c01" );
    }
}
