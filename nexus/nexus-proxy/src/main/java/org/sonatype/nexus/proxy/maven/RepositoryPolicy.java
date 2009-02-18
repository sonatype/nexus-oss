/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
    },

    MIXED
    {
        public DocumentFilter getFilter()
        {
            return new DocumentFilter()
            {
                public boolean accept( Document doc )
                {
                    return true;
                }
            };
        }
    };

    public abstract DocumentFilter getFilter();
}
