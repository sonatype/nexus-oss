package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import com.extjs.gxt.ui.client.data.ModelData;

public final class RepoServerUtil {
    
    public static String toRepositoryId(String resourceURI) {
        return resourceURI.substring(resourceURI.lastIndexOf('/') + 1);
    }

    public static String getRepositoryId(ModelData repoModel) {
        String uri = (String) repoModel.get("resourceURI");
        return toRepositoryId(uri);
    }
    
}
