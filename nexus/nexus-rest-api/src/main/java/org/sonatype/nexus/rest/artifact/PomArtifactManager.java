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
package org.sonatype.nexus.rest.artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Random;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.maven.GAVRequest;

public class PomArtifactManager
{
    private File tmpStorage = null;

    private File tmpPomFile = null;

    private GAVRequest gavRequest = null;

    private int state = 0;

    private static final int STATE_NONE = 0;

    private static final int STATE_FILE_STORED = 1;

    private static final int STATE_GAV_GENERATED = 2;

    private static final Random identifierGenerator = new Random();

    public PomArtifactManager( File tmpStorage )
    {
        super();

        this.tmpStorage = tmpStorage;
    }

    private long getNextIdentifier()
    {
        synchronized ( identifierGenerator )
        {
            return identifierGenerator.nextLong();
        }
    }

    public void storeTempPomFile( InputStream is )
        throws IOException
    {
        if ( STATE_NONE != state )
        {
            throw new IllegalStateException( "There is already a temporary pom file managed by this PomArtifactManager" );
        }

        tmpPomFile = new File( tmpStorage, getNextIdentifier() + ".xml" );

        tmpPomFile.deleteOnExit();

        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream( tmpPomFile );

            IOUtil.copy( is, os );

            state = STATE_FILE_STORED;
        }
        finally
        {
            IOUtil.close( is );

            IOUtil.close( os );
        }
    }

    public InputStream getTempPomFileInputStream()
        throws IOException
    {
        if ( STATE_FILE_STORED > state )
        {
            throw new IllegalStateException( "The temporary pom file has not yet been stored" );
        }

        return new FileInputStream( tmpPomFile );
    }

    public GAVRequest getGAVRequestFromTempPomFile()
        throws IOException,
            XmlPullParserException
    {
        if ( STATE_FILE_STORED > state )
        {
            throw new IllegalStateException( "The temporary pom file has not yet been stored" );
        }

        if ( STATE_GAV_GENERATED == state )
        {
            return gavRequest;
        }

        Reader reader = null;

        try
        {
            reader = ReaderFactory.newXmlReader( tmpPomFile );

            gavRequest = parsePom( reader );

            state = STATE_GAV_GENERATED;
        }
        finally
        {
            IOUtil.close( reader );
        }

        return gavRequest;
    }

    public void removeTempPomFile()
    {
        if ( STATE_FILE_STORED > state )
        {
            throw new IllegalStateException( "The temporary pom file has not yet been stored" );
        }

        tmpPomFile.delete();

        gavRequest = null;

        state = STATE_NONE;
    }

    private GAVRequest parsePom( Reader reader )
        throws IOException,
            XmlPullParserException
    {
        String groupId = null;

        String artifactId = null;

        String version = null;

        XmlPullParser parser = new MXParser();

        parser.setInput( reader );

        boolean foundRoot = false;

        boolean inParent = false;

        int eventType = parser.getEventType();

        // TODO: we should detect when we got all we need and simply stop parsing further
        // since we are neglecting other contents anyway
        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG )
            {
                if ( parser.getName().equals( "project" ) )
                {
                    foundRoot = true;
                }
                else if ( parser.getName().equals( "parent" ) )
                {
                    inParent = true;
                }
                else if ( parser.getName().equals( "groupId" ) )
                {
                    // 1st: if found project/groupId -> overwrite
                    // 2nd: if in parent, and groupId is still null, overwrite
                    if ( parser.getDepth() == 2 || ( inParent && groupId == null ) )
                    {
                        groupId = StringUtils.trim( parser.nextText() );
                    }
                }
                else if ( parser.getName().equals( "artifactId" ) )
                {
                    // 1st: if found project/artifactId -> overwrite
                    // 2nd: if in parent, and artifactId is still null, overwrite
                    if ( parser.getDepth() == 2 || ( inParent && artifactId == null ) )
                    {
                        artifactId = StringUtils.trim( parser.nextText() );
                    }
                }
                else if ( parser.getName().equals( "version" ) )
                {
                    // 1st: if found project/version -> overwrite
                    // 2nd: if in parent, and version is still null, overwrite
                    if ( parser.getDepth() == 2 || ( inParent && version == null ) )
                    {
                        version = StringUtils.trim( parser.nextText() );
                    }
                }
                else if ( !foundRoot )
                {
                    throw new XmlPullParserException( "Unrecognised tag: '" + parser.getName() + "'", parser, null );
                }
            }
            else if ( eventType == XmlPullParser.END_TAG )
            {
                if ( parser.getName().equals( "parent" ) )
                {
                    inParent = false;
                }
            }

            eventType = parser.next();
        }

        return new GAVRequest( groupId, artifactId, version );
    }
}
