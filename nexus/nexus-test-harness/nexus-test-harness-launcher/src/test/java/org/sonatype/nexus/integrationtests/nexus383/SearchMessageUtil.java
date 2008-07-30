package org.sonatype.nexus.integrationtests.nexus383;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class SearchMessageUtil {

	private String baseNexusUrl;

	public SearchMessageUtil(String baseNexusUrl) {
		super();
		this.baseNexusUrl = baseNexusUrl;
	}

	@SuppressWarnings("unchecked")
	public List<NexusArtifact> searchFor(String query) throws Exception {
		String serviceURI = this.baseNexusUrl + "service/local/data_index?q="
				+ query;
		System.out.println("serviceURI: " + serviceURI);

		URL serviceURL = new URL(serviceURI);

		InputStream is = serviceURL.openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int readChar = -1;
		while ((readChar = is.read()) != -1) {
			out.write(readChar);
		}

		String responseText = out.toString();
		System.out.println("responseText: \n" + responseText);
		XStream stream = new XStream();
		stream.alias("artifact", NexusArtifact.class);
		XStreamRepresentation representation = new XStreamRepresentation(
				stream, responseText, MediaType.APPLICATION_XML);

		SearchResponse searchResponde = (SearchResponse) representation
				.getPayload(new SearchResponse());

		return searchResponde.getData();
	}

}
