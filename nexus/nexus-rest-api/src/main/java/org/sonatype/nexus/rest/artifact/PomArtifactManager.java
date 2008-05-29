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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Random;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.maven.GAVRequest;

public class PomArtifactManager
{    
    private File tmpPomFile = null;
    private GAVRequest gavRequest = null;    
    private int state = 0;
    
    private static final int STATE_NONE = 0;
    private static final int STATE_FILE_STORED = 1;
    private static final int STATE_GAV_GENERATED = 2;    
    private static final Random identifierGenerator = new Random();
    
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
        
        tmpPomFile = new File( System.getProperty( "java.io.tmpdir" ), getNextIdentifier() + ".xml" );
        tmpPomFile.deleteOnExit();
        
        FileWriter fw = null;
        try
        {
            fw = new FileWriter( tmpPomFile );
            InputStreamReader reader = new InputStreamReader( is );
            char[] buf = new char[256];
            int read = 0;
            while ((read = reader.read(buf)) > 0) {
                fw.write(buf, 0, read);
            }
            fw.flush();
            state = STATE_FILE_STORED;
        }
        finally
        {
            if ( fw != null )
            {
                fw.close();
            }
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
        throws IOException, XmlPullParserException
    {
        if ( STATE_FILE_STORED > state )
        {
            throw new IllegalStateException( "The temporary pom file has not yet been stored" );
        }
        
        if ( STATE_GAV_GENERATED == state)
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
            if ( reader != null )
            {
                reader.close();
            }
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
        throws IOException, XmlPullParserException
    {
        String groupId = null;
        String artifactId = null;
        String version = null;
        
        XmlPullParser parser = new MXParser();
        parser.setInput( reader );
     
        boolean foundRoot = false;
        int eventType = parser.getEventType();
        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG )
            {
                if ( parser.getName().equals( "project" ) )
                {                 
                    foundRoot = true;
                }
                else if ( parser.getName().equals( "groupId" ) )
                {                 
                    groupId = StringUtils.trim( parser.nextText() );
                }
                else if ( parser.getName().equals( "artifactId" ) )
                {   
                    artifactId = StringUtils.trim( parser.nextText() );
                }
                else if ( parser.getName().equals( "version" ) )
                {                 
                    version = StringUtils.trim( parser.nextText() );
                }
                else if ( !foundRoot )
                {
                    throw new XmlPullParserException( "Unrecognised tag: '" + parser.getName() + "'", parser, null);
                }
            }
            eventType = parser.next();
        }
        
        return new GAVRequest( groupId, artifactId, version );
    }
}
