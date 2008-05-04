/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.store.file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.store.DefaultEntry;
import org.sonatype.nexus.store.Entry;
import org.sonatype.nexus.store.Store;

public class FileStoreTest
    extends AbstractNexusTestCase
{
    protected FileStore fileStore;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        fileStore = (FileStore) lookup( Store.ROLE, "file" );

        fileStore.getBaseDir();
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

    }

    protected String getStoreDir()
    {
        return getBasedir() + "/target/plexus-home/work/nexus/template-store";
    }

    protected void createDefaultTwoObject()
        throws IOException
    {
        FileUtils
            .fileWrite(
                getStoreDir() + "/simpleObject.xml",
                "<org.sonatype.nexus.store.file.SimpleObject><aString>This is a string!</aString><anInt>1975</anInt><aBoolean>true</aBoolean></org.sonatype.nexus.store.file.SimpleObject>" );

        FileUtils.fileWrite( getStoreDir() + "/test.txt.xml", "<string>This is a string!</string>" );
    }

    public void testAddEntry()
        throws Exception
    {
        File file = new File( getBasedir(), "target/tmp/test.txt" );

        file.getParentFile().mkdirs();

        FileUtils.fileWrite( file.getAbsolutePath(), "<string>test</string>" );

        DefaultEntry entry = new DefaultEntry( "test.txt", "test" );

        fileStore.addEntry( entry );

        File newFile = new File( getStoreDir(), "test.txt.xml" );

        assertTrue( newFile.exists() );

        assertTrue( FileUtils.contentEquals( file, newFile ) );

        SimpleObject simpleObject = new SimpleObject();

        simpleObject.setABoolean( true );

        simpleObject.setAnInt( 1975 );

        simpleObject.setAString( "This is a string!" );

        entry = new DefaultEntry( "simpleObject", simpleObject );

        fileStore.addEntry( entry );
    }

    public void testGetEntries()
        throws IOException
    {
        createDefaultTwoObject();

        Collection<Entry> entries = fileStore.getEntries();

        assertEquals( 2, entries.size() );

        // the order is undefined
        for ( Iterator<Entry> i = entries.iterator(); i.hasNext(); )
        {
            Entry e = i.next();

            if ( "simpleObject".equals( e.getId() ) )
            {
                assertEquals( "simpleObject", e.getId() );
                SimpleObject simpleObject = (SimpleObject) e.getContent();
                assertEquals( 1975, simpleObject.getAnInt() );
                assertEquals( true, simpleObject.isABoolean() );
                assertEquals( "This is a string!", simpleObject.getAString() );
            }
            else if ( "test.txt".equals( e.getId() ) )
            {
                assertEquals( "test.txt", e.getId() );
                String string = (String) e.getContent();
                assertEquals( "This is a string!", string );
            }
            else
            {
                fail( "This entry with ID=" + e.getId() + " should not be in Store!" );
            }
        }
    }

    public void testGetEntry()
    {
        // fail( "Not yet implemented" );
    }

    public void testRemoveEntry()
        throws IOException
    {
        createDefaultTwoObject();

        Collection<Entry> entries = fileStore.getEntries();

        assertEquals( 2, entries.size() );

        fileStore.removeEntry( "test.txt" );

        entries = fileStore.getEntries();

        assertEquals( 1, entries.size() );

        Entry e1 = entries.iterator().next();

        assertEquals( "simpleObject", e1.getId() );
        SimpleObject simpleObject = (SimpleObject) e1.getContent();
        assertEquals( 1975, simpleObject.getAnInt() );
        assertEquals( true, simpleObject.isABoolean() );
        assertEquals( "This is a string!", simpleObject.getAString() );
    }

    public void testUpdateEntry()
        throws IOException
    {
        createDefaultTwoObject();

        Entry e = fileStore.getEntry( "simpleObject" );

        assertEquals( "simpleObject", e.getId() );
        SimpleObject simpleObject = (SimpleObject) e.getContent();
        assertEquals( 1975, simpleObject.getAnInt() );
        assertEquals( true, simpleObject.isABoolean() );
        assertEquals( "This is a string!", simpleObject.getAString() );

        simpleObject.setABoolean( false );
        simpleObject.setAnInt( 1982 );
        simpleObject.setAString( "Just joking!" );

        fileStore.updateEntry( e );

        e = fileStore.getEntry( "simpleObject" );

        assertEquals( "simpleObject", e.getId() );
        simpleObject = (SimpleObject) e.getContent();
        assertEquals( 1982, simpleObject.getAnInt() );
        assertEquals( false, simpleObject.isABoolean() );
        assertEquals( "Just joking!", simpleObject.getAString() );
    }

}
