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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;

import org.sonatype.nexus.proxy.AbstractNexusTestCase;

/**
 * @author Juven Xu
 */
public class DefaultFSWastebasketTest
    extends AbstractNexusTestCase
{
    DefaultFSWastebasketForTest wastebasket;
    
    private File trashDir = null;
    private File wastebasketDir = null;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        trashDir = new File( getPlexusHomeDir(), "trash" );
        trashDir.mkdir();
        
        wastebasketDir = new File( getPlexusHomeDir(), "wastebasket" );
        wastebasketDir.mkdir();

        wastebasket = new DefaultFSWastebasketForTest();
    }
    
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        
        cleanDir( trashDir );
        cleanDir( wastebasketDir );
    }

    public void testDeleteFile()
        throws Exception
    {
        new File( wastebasketDir, "folderB" ).mkdirs();
        new File( wastebasketDir, "folderA/folderAA" ).mkdirs();
        new File( wastebasketDir, "folderA/folderAA/fileA.txt" ).createNewFile();
        new File( wastebasketDir, "folderB/fileB.txt" ).createNewFile();

        wastebasket.delete( wastebasketDir, false );

        File fileA = new File( trashDir, "wastebasket/folderA/folderAA/fileA.txt" );
        File fileB = new File( trashDir, "wastebasket/folderB/fileB.txt" );
        File folderAA = new File( trashDir, "wastebasket/folderA/folderAA" );

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
            return trashDir;
        }
    }
}
