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

package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

/**
 * @author Juven Xu
 */
public class WastebasketTest
    extends TestCase
{
    Wastebasket wastebasket;

    public void setUp()
        throws Exception
    {
        File trashDir = new File( "target/trash" );

        FileUtils.deleteDirectory( trashDir );

        trashDir.mkdir();

        wastebasket = new DefaultFSWastebasketForTest();
    }

    public void testDeleteFile()
        throws Exception
    {
        new File( "target/wastebasket" ).mkdir();
        new File( "target/wastebasket/folderA" ).mkdir();
        new File( "target/wastebasket/folderB" ).mkdir();
        new File( "target/wastebasket/folderA/folerAA" ).mkdir();
        new File( "target/wastebasket/folderA/folerAA/fileA.txt" ).createNewFile();
        new File( "target/wastebasket/folderB/fileB.txt" ).createNewFile();

        File toBeDeleted = new File( "target/wastebasket" );

        wastebasket.delete( toBeDeleted );

        File fileA = new File( "target/trash/wastebasket/folderA/folerAA/fileA.txt" );
        File fileB = new File( "target/trash/wastebasket/folderB/fileB.txt" );
        File folderAA = new File( "target/trash/wastebasket/folderA/folerAA" );

        assertTrue( fileA.exists() && fileA.isFile() );
        assertTrue( fileB.exists() && fileB.isFile() );
        assertTrue( folderAA.exists() && folderAA.isDirectory() );
    }

    class DefaultFSWastebasketForTest
        extends DefaultFSWastebasket
    {
        /**
         * Override for supplying a test trash directory
         */
        @Override
        public File getWastebasketDirectory()
        {
            return new File( "target/trash" );
        }
    }
}
