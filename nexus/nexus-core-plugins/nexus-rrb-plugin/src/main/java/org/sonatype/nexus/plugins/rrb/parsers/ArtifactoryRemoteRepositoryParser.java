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
package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class ArtifactoryRemoteRepositoryParser extends
		HtmlRemoteRepositoryParser
{
	private final Logger logger = LoggerFactory.getLogger( ArtifactoryRemoteRepositoryParser.class );

	/** Links to sub-repos contain the pattern assigned to artifactoryLinkPattern
	e.g. as in
		<a class="icon-link folder" href="http://repo.jfrog.org/artifactory/java.net-cache/args4j/">args4j</a>
	 or
		<a class="icon-link xml" href="http://repo.jfrog.org/artifactory/java.net-cache/archetype-catalog.xml">archetype-catalog.xml</a>
	*/
	static String artifactoryLinkPattern = "class=\"icon-link";
	static String startOfArtifactoryLink = "<a " + artifactoryLinkPattern;
	static String validRefStart = "href=\"http";
	static String folderLink = artifactoryLinkPattern + " folder";
	static String folderLinkMatchPattern = ".*" + folderLink + ".*";
	//static String xmlLink = artifactoryLinkPattern + " xml";
	//static String pomLink = artifactoryLinkPattern + " pom";
	static String uriPrefixEnd = "/http";
	
	public ArtifactoryRemoteRepositoryParser(String remotePath, String localUrl,
			String id, String baseUrl)
	{
		super(remotePath, localUrl, id, baseUrl);		    
	}
	
	@Override
	public ArrayList<RepositoryDirectory> extractLinks( StringBuilder indata )
	{
        ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();
        ArrayList<String> artifactoryLinks = extractArtifactoryLinks( indata );
        
        int uriPrefixEndPosition = localUrl.indexOf(uriPrefixEnd);
        String uriPrefix = "";
        if( uriPrefixEndPosition > 0 ) {
           uriPrefix = localUrl.substring( 0, uriPrefixEndPosition ) + "/";
        }
        for (String artifactoryLink : artifactoryLinks) {
        	RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
        	String text = getLinkName( artifactoryLink ).replace( "/", "" ).trim();      	
        	//If the link not contains the folderLink string it is a leaf
         	repositoryDirectory.setLeaf( !artifactoryLink.matches(folderLinkMatchPattern));
        	repositoryDirectory.setText( text );        	
        	repositoryDirectory.setResourceURI( getLinkUrl( artifactoryLink ) );
        	repositoryDirectory.setRelativePath( getRelativePath( artifactoryLink ) ); 
        	result.add( repositoryDirectory ); 
		}
        return result;
    }
	
	/**
	 * Go through the indata (i.e. the html content) and extract the links (i.e. <a.../a>) that are of Artifactory type
	 * @param indata-the html content
	 * @return an ArrayList<String> where each element is a html anchor-link 
	 */
	public ArrayList<String> extractArtifactoryLinks(StringBuilder indata)
	{
		ArrayList<String> result = new ArrayList<String>();
		int currentStartPosition = -1;
		int endPosition = currentStartPosition;
		while((currentStartPosition = getNextArtifactoryAnchorPosition( indata, endPosition )) > 0) {
		    endPosition = indata.indexOf( linkEnd, currentStartPosition ) + linkEnd.length();
			String string = indata.substring( currentStartPosition, endPosition );
			if( containsValidArtifactoryReference( string )) {
				result.add( string );
			}
		}
		return result;
	}

	/**
	 * Check if string contains a reference defined by validRefStart
	 * but exclude the reference if the anchor text contains ">..<"   
	 * @param string - the anchor text
	 * @return
	 */
	private boolean containsValidArtifactoryReference(String string)
	{
		return (string.indexOf(validRefStart) > 0) && (string.indexOf(">..<") < 0);
	}

	/**
	 * Get the next anchor of Artifactory type starting in the last end position 
	 * @param indata - the indata text to search
	 * @param lastEndPosition - the position of the end of last search 
	 * @return
	 */
	private int getNextArtifactoryAnchorPosition(StringBuilder indata,
			int lastEndPosition)
	{
		return indata.indexOf( startOfArtifactoryLink, lastEndPosition );
	}	
	
    protected String getLinkName( String anchorString )
    {
        return getLinkName( new StringBuilder( anchorString ) );
    }

    private String getRelativePath( String anchorString )
    {
        String artifactoryUrl = getLinkUrl( new StringBuilder( anchorString ) );
        
        return artifactoryUrl.substring( this.baseUrl.length() );
    }
    
    protected String getLinkUrl( String anchorString )
    {   
        String relativePath = this.getRelativePath( anchorString );

        // strip starting /
        if( relativePath.startsWith( "/" ) )
        {
            relativePath = relativePath.substring( 1 );
        }
        
        String url;
        if( !this.localUrl.endsWith( "/" ) )
        {
            url = this.localUrl + "/" + relativePath;
        }
        else
        {
            url = this.localUrl + relativePath;
        }
        
        return url;
        
    }

}
