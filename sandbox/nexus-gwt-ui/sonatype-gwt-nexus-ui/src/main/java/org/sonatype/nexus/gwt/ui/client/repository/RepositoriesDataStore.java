package org.sonatype.nexus.gwt.ui.client.repository;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.gwt.ui.client.data.JSONArrayDataStore;

import com.google.gwt.json.client.JSONArray;

/**
 *
 * @author barath
 */
public class RepositoriesDataStore extends JSONArrayDataStore {
    
    private JSONArrayDataStore proxy = new JSONArrayDataStore();
    
    private JSONArrayDataStore hosted = new JSONArrayDataStore();

    private JSONArrayDataStore virtual = new JSONArrayDataStore();

    
    public JSONArrayDataStore getProxy() {
        return proxy;
    }

    public JSONArrayDataStore getHosted() {
        return hosted;
    }

    public JSONArrayDataStore getVirtual() {
        return virtual;
    }

    public void setElements(JSONArray repos) {
        super.setElements(repos);
        
        List proxies = new ArrayList();
        List hosteds = new ArrayList();
        List virtuals = new ArrayList();
        
        for (int i = 0; i < repos.size(); i++) {
            String repoType = repos.get(i).isObject().get("repoType").isString().stringValue();
            if ("proxy".equals(repoType)) {
                proxies.add(repos.get(i));
            } else if ("hosted".equals(repoType)) {
                hosteds.add(repos.get(i));
            } else if ("virtual".equals(repoType)) {
                virtuals.add(repos.get(i));
            }
        }
        
        proxy.setElements(proxies);
        hosted.setElements(hosteds);
        virtual.setElements(virtuals);
    }

    public void setElements(List elements) {
        super.setElements(elements);
        //TODO: implement
        throw new UnsupportedOperationException("not implemented yet");
    }
    
}
