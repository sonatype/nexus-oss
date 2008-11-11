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

package org.sonatype.nexus.tools.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.maven.AbstractMetadataHelper;

@Component( role = FSMetadataHelper.class )
public class FSMetadataHelper
    extends AbstractMetadataHelper
{
    private String repo;

    @Override
    public InputStream retrieveContent( String path )
    {
        try
        {
            return new FileInputStream( repo + path );
        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( "Can't find file: " + repo + path );
        }
    }

    @Override
    public void store( String content, String path )
    {
        String file = repo + path;

        try
        {
            FileUtils.fileWrite( file, content );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Can't write content to: " + file, e );
        }

    }

    public String getRepo()
    {
        return repo;
    }

    public void setRepo( String repo )
    {
        this.repo = repo;
    }

    //copy from org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector
    @Override
    public String buildMd5( String path )
        throws Exception
    {
        InputStream fis = retrieveContent( path );
        
        try
        {
            byte[] buffer = new byte[1024];

            MessageDigest md5 = MessageDigest.getInstance( "MD5" );

            int numRead;

            do
            {
                numRead = fis.read( buffer );
                
                if ( numRead > 0 )
                {
                    md5.update( buffer, 0, numRead );
                }
            }
            while ( numRead != -1 );

            return new String( Hex.encodeHex( md5.digest() ) );

        }
        finally
        {
            fis.close();
        }
    }

    @Override
    public String buildSh1( String path )
        throws Exception
    {
        InputStream fis = retrieveContent( path );
        
        try
        {
            byte[] buffer = new byte[1024];

            MessageDigest sha1 = MessageDigest.getInstance( "SHA1" );

            int numRead;

            do
            {
                numRead = fis.read( buffer );
                
                if ( numRead > 0 )
                {
                    sha1.update( buffer, 0, numRead );

                }
            }
            while ( numRead != -1 );

            return new String( Hex.encodeHex( sha1.digest() ) );

        }
        finally
        {
            fis.close();
        }
    }
    
}


