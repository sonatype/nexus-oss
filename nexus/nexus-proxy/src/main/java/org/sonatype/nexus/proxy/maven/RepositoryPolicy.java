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
package org.sonatype.nexus.proxy.maven;

import org.apache.lucene.document.Document;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.DocumentFilter;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;

public enum RepositoryPolicy
{
    RELEASE 
    {
        public DocumentFilter getFilter() 
        {
            return new DocumentFilter()
            {
                public boolean accept( Document doc )
                {
                    String uinfo = doc.get( ArtifactInfo.UINFO );
    
                    if ( uinfo == null ) 
                    {
                        return true;
                    } 
    
                    String[] r = AbstractIndexCreator.FS_PATTERN.split( uinfo );
    
                    return !VersionUtils.isSnapshot( r[2] );
                }
            };         
        }
    },

    SNAPSHOT 
    {
        public DocumentFilter getFilter() 
        {
            return new DocumentFilter()
            {
                public boolean accept( Document doc )
                {
                    String uinfo = doc.get( ArtifactInfo.UINFO );
    
                    if ( uinfo == null ) 
                    {
                        return true;
                    } 
    
                    String[] r = AbstractIndexCreator.FS_PATTERN.split( uinfo );
    
                    return VersionUtils.isSnapshot( r[2] );
                }
            };         
        }
    };

    public abstract DocumentFilter getFilter();
}
