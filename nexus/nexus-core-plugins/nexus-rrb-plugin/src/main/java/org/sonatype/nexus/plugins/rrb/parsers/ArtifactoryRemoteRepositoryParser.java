package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class ArtifactoryRemoteRepositoryParser extends
		HtmlRemoteRepositoryParser {
	private final Logger logger = LoggerFactory.getLogger( ArtifactoryRemoteRepositoryParser.class );

	String startOftArtifactoryLink = "<a class=\"icon-link";
	String validRefStart = "href=\"http";
	String folderLink = "class=\"icon-link folder";
	String folderLinkMatchPattern = ".*" + folderLink + ".*";
	String xmlLink = "class=\"icon-link xml";
	String pomLink = "class=\"icon-link pom";
	String uriPrefixEnd = "/http";

	public ArtifactoryRemoteRepositoryParser(String remoteUrl, String localUrl,
			String id, String baseUrl) {
		super(remoteUrl, localUrl, id, baseUrl);
	}
	
    String getLinkName( String temp )
    {
        int start = temp.indexOf( ">" ) + 1;
        int end = temp.indexOf( "</" );
        return cleanup( temp.substring( start, end ) );
    }

    String getLinkUrl( String temp )
    {
        int start = temp.indexOf( href ) + href.length();
        int end = temp.indexOf( "\"", start + 1 );
        return temp.substring( start, end );
    }

	@Override
	public ArrayList<RepositoryDirectory> extractLinks( StringBuilder indata ) {

        ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();
        ArrayList<String> artifactoryLinks = extractArtifactoryLinks( indata );
        
        int uriPrefixEndPosition = localUrl.indexOf(uriPrefixEnd);
        String uriPrefix = "";
        if(uriPrefixEndPosition > 0 ) {
           uriPrefix = localUrl.substring(0, uriPrefixEndPosition) + "/";
        }
        for (String artifactoryLink : artifactoryLinks) {
        	RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
        	String text = getLinkName( artifactoryLink ).replace( "/", "" ).trim();      	
        	//If the link not contains the folderLink string it is a leaf
         	repositoryDirectory.setLeaf( !artifactoryLink.matches(folderLinkMatchPattern));
        	repositoryDirectory.setText(text);        	
        	repositoryDirectory.setResourceURI(uriPrefix + getLinkUrl( artifactoryLink ) );
        	repositoryDirectory.setRelativePath(getLinkUrl( artifactoryLink )); 
        	result.add( repositoryDirectory ); 
		}
        return result;
    }
	
	/**
	 * Go through the indata (i.e. the html content) and extract the links (i.e. <a.../a>) that are of Artifactory types
	 * @param indata-the html content
	 * @return an ArrayList<String> where each element is a html anchor-link 
	 */
	public ArrayList<String> extractArtifactoryLinks(StringBuilder indata) {
		ArrayList<String> result = new ArrayList<String>();
		int currentStartPosition = -1;
		while((currentStartPosition = indata.indexOf("<a class=\"icon-link", currentStartPosition + 1)) > 0) {
			int end = indata.indexOf( linkEnd, currentStartPosition ) + linkEnd.length();
			String string = indata.substring( currentStartPosition, end );
			if( (string.indexOf(validRefStart) > 0) && (string.indexOf(">..<") < 0)) result.add( string );
		}
		return result;
	}

}
