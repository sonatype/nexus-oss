/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.junit.Test;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;

public class PrefixesFileMarshallerTest
{
    // is state-less, no need for @Before
    final PrefixesFileMarshaller m = new PrefixesFileMarshaller();

    final Charset UTF8 = Charset.forName( "UTF-8" );

    protected String prefixFile1( boolean withComments )
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter( sw );
        if ( withComments )
        {
            pw.println( "# This is mighty prefix file!" );
        }
        pw.println( "/org/apache/maven" );
        pw.println( "/org/sonatype" );
        if ( withComments )
        {
            pw.println( "# Added later" );
        }
        pw.println( "/eu/flatwhite" );
        return sw.toString();
    }

    @Test
    public void roundtrip()
        throws IOException
    {
        final EntrySource readEntrySource = m.read( new ByteArrayInputStream( prefixFile1( true ).getBytes( UTF8 ) ) );
        assertThat( readEntrySource.exists(), is( true ) );
        assertThat( readEntrySource.readEntries().size(), is( 3 ) );

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        m.write( readEntrySource, outputStream );
        assertThat( outputStream.size(), greaterThan( 15 ) );

        final String output = new String( outputStream.toByteArray(), UTF8 );
        assertThat( output, equalTo( prefixFile1( false ) ) );
    }
}
