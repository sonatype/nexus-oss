package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class RepositoryStatus {
    
    private String id;
    
    private String repoType;
    
    private String localStatus;
    
    private String remoteStatus;
    
    private String proxyMode;
    
    public RepositoryStatus() {
    }

    public RepositoryStatus(ModelData model) {
        String uri = (String) model.get("resourceURI");
        setId(uri.substring(uri.lastIndexOf('/') + 1));
        setRepoType((String) model.get("repoType"));
        setLocalStatus((String) model.get("localStatus"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getLocalStatus() {
        return localStatus;
    }

    public void setLocalStatus(String localStatus) {
        this.localStatus = localStatus;
    }
    
    public String getRemoteStatus() {
        return remoteStatus;
    }

    public void setRemoteStatus(String remoteStatus) {
        this.remoteStatus = remoteStatus;
    }

    public String getProxyMode() {
        return proxyMode;
    }

    public void setProxyMode(String proxyMode) {
        this.proxyMode = proxyMode;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<id>");
        sb.append(id);
        sb.append("</id>");
        sb.append("<repoType>");
        sb.append(repoType);
        sb.append("</repoType>");
        sb.append("<localStatus>");
        sb.append(localStatus);
        sb.append("</localStatus>");
        return sb.toString();
    }

    public void fromXML(String xml) {
        Document doc = XMLParser.parse(xml);
        NodeList nodes = doc.getFirstChild().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ("id".equals(node.getNodeName())) {
                id = node.getFirstChild().getNodeValue();
            } else if ("repoType".equals(node.getNodeName())) {
                repoType = node.getFirstChild().getNodeValue();
            } else if ("localStatus".equals(node.getNodeName())) {
                localStatus = node.getFirstChild().getNodeValue();
            } else if ("remoteStatus".equals(node.getNodeName())) {
                remoteStatus = node.getFirstChild().getNodeValue();
            } else if ("proxyMode".equals(node.getNodeName())) {
                proxyMode = node.getFirstChild().getNodeValue();
            }
        }
    }

}
