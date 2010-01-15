package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class HtmlRemoteRepositoryParser implements RemoteRepositoryParser {

	private final Logger logger = LoggerFactory
			.getLogger(HtmlRemoteRepositoryParser.class);
	private static final String[] EXCLUDES = { ">Skip to content<", ">Log in<",
			">Products<", "Parent Directory", "?", "..", "-logo.png",">Community<",
			">Support<",">Resources<",">About us<",">Downloads<",">Documentation<",">Resources<",
			">About This Site<",">Contact Us<",">Legal Terms and Privacy Policy<",">Log out<",
			">IONA Technologies<",">Site Index<",">Skip to content<"};
	private String localUrl;
	private String remoteUrl;

	private String linkStart = "<a ";
	private String linkEnd = "/a>";
	private String href = "href=\"";
	private String id;

	public HtmlRemoteRepositoryParser(String remoteUrl, String localUrl, String id) {
		this.remoteUrl = remoteUrl;
		this.localUrl = localUrl;
		this.id=id;
	}

	/**
	 * Extracts the links and sets the data in the RepositoryDirectory object.
	 * 
	 * @param indata
	 * @return a list of RepositoryDirectory objects
	 */
	public ArrayList<RepositoryDirectory> extractLinks(StringBuilder indata) {
		ArrayList<RepositoryDirectory> result = new ArrayList<RepositoryDirectory>();

		if (indata.indexOf(linkStart.toUpperCase()) != -1) {
			linkStart = linkStart.toUpperCase();
			linkEnd = linkEnd.toUpperCase();
			href = href.toUpperCase();
		}
		int start = 0;
		int end = 0;
		do {
			RepositoryDirectory rp = new RepositoryDirectory();
			StringBuilder temp = new StringBuilder();
			start = indata.indexOf(linkStart, start);
			if (start < 0) {
				break;
			}
			end = indata.indexOf(linkEnd, start) + linkEnd.length();
			temp.append(indata.subSequence(start, end));
			if (!exclude(temp)) {
				if (!getLinkName(temp).endsWith("/")) {
					rp.setLeaf(true);
				}
				rp.setText(getLinkName(temp).replace("/", "").trim());
				if(!remoteUrl.endsWith("/")){
					remoteUrl+="/";
				}
				rp.setResourceURI(localUrl + "?remoteurl=" + remoteUrl
						+ getLinkUrl(temp)+"?id="+id);
				rp.setRelativePath("/" + getLinkUrl(temp));

				if(!rp.getText().isEmpty()){
					result.add(rp);
				}
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
		return cleanup(temp.substring(start, end));
	}

	private String cleanup(String value) {
		int start=value.indexOf('<');
		int end= value.indexOf('>');
		if(start!=-1&&start<end){
			CharSequence seq = value.substring(start,end+1);
			value=value.replace(seq, "");
			cleanup(value);
		}
		return value;
	}

	/**
	 * Extracts the link url.
	 */
	private String getLinkUrl(StringBuilder temp) {
		int start = temp.indexOf(href) + href.length();
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
