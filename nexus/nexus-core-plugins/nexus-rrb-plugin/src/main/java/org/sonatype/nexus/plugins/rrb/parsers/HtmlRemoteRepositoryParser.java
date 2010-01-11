package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class HtmlRemoteRepositoryParser implements RemoteRepositoryParser {

	final Logger logger = LoggerFactory.getLogger(HtmlRemoteRepositoryParser.class);
    private static final String[] EXCLUDES = { "Parent Directory", "?", ".." };
    private String localUrl;
    private String remoteUrl;

    public HtmlRemoteRepositoryParser(String remoteUrl, String localUrl) {
        this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
    }

    /**
     * Extracts the links and sets the data in the RepositoryDirectory object.
     * 
     * @param indata
     * @return a list of RepositoryDirectory objects
     */
    public ArrayList<RepositoryDirectory> extractLinks(StringBuilder indata) {
        ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();
        int start = 0;
        int end = 0;
        do {
            RepositoryDirectory rp = new RepositoryDirectory();
            StringBuilder temp = new StringBuilder();
            start = indata.indexOf("<a ", start);
            if (start < 0) {
                break;
            }
            end = indata.indexOf("/a>", start) + 3;
            temp.append(indata.subSequence(start, end));
            if (!exclude(temp)) {
                if (!getLinkName(temp).endsWith("/")) {
                    rp.setLeaf(true);
                }
                rp.setText(getLinkName(temp).replace("/", ""));
                rp.setResourceURI(localUrl + "?remoteurl=" + remoteUrl + getLinkUrl(temp));
                rp.setRelativePath("/" + getLinkUrl(temp));

                result.add(rp);
                logger.debug("addning {} to result", rp.toString());
            }
            start = end + 1;
        } while (start > 0);

        return result;
    }

    /**
     * Extracts the link name.
     */
    private String getLinkName(StringBuilder temp) {
        int start = temp.indexOf(">") + 1;
        int end = temp.indexOf("</");
        return temp.substring(start, end);
    }

    /**
     * Extracts the link url.
     */
    private String getLinkUrl(StringBuilder temp) {
        int start = temp.indexOf("href=\"") + 6;
        int end = temp.indexOf("\"", start + 1);
        return temp.substring(start, end);
    }

    /**
     * Excludes links that are not relevant for the listing.
     */
    private boolean exclude(StringBuilder value) {
        for (String s : EXCLUDES) {
            if (value.indexOf(s) > 0) {
            	logger.debug("{} is in EXCLUDES array", value);
                return true;
            }
        }
        return false;
    }
}
