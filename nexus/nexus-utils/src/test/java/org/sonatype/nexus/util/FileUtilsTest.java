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
package org.sonatype.nexus.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTest
{
    @Test
    public void testUNCPath()
        throws Exception
    {
        if ( FileUtils.isWindows() )
        {
            String filepath = "\\\\someserver\blah\blah\blah.jar";
            Assert.assertTrue( FileUtils.validFileUrl( filepath ) );

            File file = new File( filepath );
            Assert.assertTrue( FileUtils.validFile( file ) );

            String badFilepath = "someserver\blah\blah\blah.jar";
            Assert.assertFalse( FileUtils.validFileUrl( badFilepath ) );

            String urlFilepath = "file:////someserver/blah/blah.jar";
            Assert.assertTrue( FileUtils.validFileUrl( filepath ) );

            Assert.assertTrue( FileUtils.validFile( new File( new URL( urlFilepath ).getFile() ) ) );
        }
        else
        {
            Assert.assertTrue( true );
        }
    }

    @Test
    public void testMove()
        throws Exception
    {
        File root = org.codehaus.plexus.util.FileUtils.createTempFile( "dir", "tmp", null );
        File dest = new File( root, "dest" );
        dest.mkdirs();

        {
            File dir = new File( root, "a/b/c/d/e" );
            dir.mkdirs();
            FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
            Assert.assertTrue( new File( dest, "a/b/c/d/e" ).isDirectory() );
            Assert.assertFalse( dir.exists() );
            Assert.assertFalse( new File( root, "a" ).exists() );
        }

        {
            File file = new File( root, "a/file.txt" );
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
            Assert.assertTrue( new File( dest, "a/b/c/d/e" ).isDirectory() );
            Assert.assertTrue( new File( dest, "a/file.txt" ).isFile() );
            Assert.assertFalse( file.exists() );
            Assert.assertFalse( new File( root, "a" ).exists() );
        }

        {
            File dir2 = new File( root, "a/another" );
            dir2.mkdirs();
            FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
            Assert.assertTrue( new File( dest, "a/b/c/d/e" ).isDirectory() );
            Assert.assertTrue( new File( dest, "a/file.txt" ).isFile() );
            Assert.assertTrue( new File( dest, "a/another" ).isDirectory() );
            Assert.assertFalse( dir2.exists() );
            Assert.assertFalse( new File( root, "a" ).exists() );
        }

        {
            File file = new File( root, "a/file.txt/i/t" );
            file.mkdirs();
            FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
            Assert.assertTrue( new File( dest, "a/b/c/d/e" ).isDirectory() );
            Assert.assertTrue( new File( dest, "a/file.txt/i/t" ).isDirectory() );
            Assert.assertFalse( file.exists() );
            Assert.assertFalse( new File( root, "a" ).exists() );
        }

        {
            File file = new File( root, "a/b/c/d" );
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
            Assert.assertTrue( new File( dest, "a/b/c/d" ).isFile() );
            Assert.assertFalse( new File( dest, "a/b/c/d/e" ).exists() );
            Assert.assertFalse( file.exists() );
            Assert.assertFalse( new File( root, "a" ).exists() );
        }

        try
        {
            FileUtils.move( null, new File( dest, "a" ) );
            Assert.fail();
        }
        catch ( NullPointerException e )
        {
            Assert.assertTrue( e.getMessage().contains( "source" ) );
        }

        try
        {
            FileUtils.move( new File( root, "a" ), null );
            Assert.fail();
        }
        catch ( NullPointerException e )
        {
            Assert.assertTrue( e.getMessage().contains( "destination" ) );
        }

        try
        {
            FileUtils.move( new File( root, "j" ), new File( dest, "j" ) );
            Assert.fail();
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( e.getMessage().contains( "Source file doesn't exist" ) );
        }

        // *nix doesn't lock files
        if ( FileUtils.isWindows() )
        {
            {
                File file = new File( root, "l/k.txt" );
                file.getParentFile().mkdirs();
                file.createNewFile();

                FileChannel channel = new RandomAccessFile( file, "rw" ).getChannel();
                FileLock lock = channel.lock();

                try
                {
                    FileUtils.move( new File( root, "l" ), new File( dest, "l" ) );
                    Assert.fail();
                }
                catch ( IOException e )
                {
                    Assert.assertTrue( e.getMessage().contains( "Failed to move" ) );
                }
                finally
                {

                    lock.release();
                    channel.close();

                    org.codehaus.plexus.util.FileUtils.forceDelete( file );
                }
            }

            {
                File file = new File( root, "a/b/c/d" );
                file.getParentFile().mkdirs();
                file.createNewFile();

                FileChannel channel = new RandomAccessFile( file, "rw" ).getChannel();
                FileLock lock = channel.lock();

                try
                {
                    FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
                    Assert.fail();
                }
                catch ( IOException e )
                {
                    Assert.assertTrue( e.getMessage().contains( "Failed to move" ) );
                }
                finally
                {

                    lock.release();
                    channel.close();

                    org.codehaus.plexus.util.FileUtils.forceDelete( file );
                }
            }

            {
                File file = new File( root, "a/b/c/d/e/f.txt" );
                file.getParentFile().mkdirs();
                file.createNewFile();

                FileChannel channel = new RandomAccessFile( file, "rw" ).getChannel();
                FileLock lock = channel.lock();

                try
                {
                    FileUtils.move( new File( root, "a" ), new File( dest, "a" ) );
                    Assert.fail();
                }
                catch ( IOException e )
                {
                    Assert.assertTrue( e.getMessage().contains( "Failed to move" ) );
                }
                finally
                {

                    lock.release();
                    channel.close();

                    org.codehaus.plexus.util.FileUtils.forceDelete( file );
                }
            }
        }
    }
}
