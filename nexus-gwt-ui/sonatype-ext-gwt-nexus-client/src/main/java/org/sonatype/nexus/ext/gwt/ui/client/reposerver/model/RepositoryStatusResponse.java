package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class RepositoryStatusResponse {
    
    private RepositoryStatus data;
    
    public RepositoryStatusResponse() {
    }

    public RepositoryStatusResponse(RepositoryStatus data) {
        this.data = data;
    }

    public RepositoryStatus getData() {
        return data;
    }

    public void setData(RepositoryStatus data) {
        this.data = data;
    }
    
    public String toXML() {
        return "<org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse>" +
               "<data>" + data.toXML() + "</data>" +
               "</org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse>";
    }
    
    public void fromXML(String xml) {
        Document doc = XMLParser.parse(xml);
        data = new RepositoryStatus();
        data.fromXML(doc.getElementsByTagName("data").toString());
    }

}
