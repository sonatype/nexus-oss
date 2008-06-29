package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import org.sonatype.nexus.ext.gwt.ui.client.data.Entity;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class AuthenticationClientPermissions extends BaseModelData implements Entity {

    public String getType() {
        return "org.sonatype.nexus.rest.model.AuthenticationClientPermissions";
    }

    public Class getFieldType(String fieldName) {
        return Integer.class;
    }

    public Entity createEntity(String fieldName) {
        return null;
    }

    public Integer getViewSearch() {
        return get("viewSearch");
    }

    public void setViewSearch(Integer viewSearch) {
        set("viewSearch", viewSearch);
    }

    public Integer getViewUpdatedArtifacts() {
        return get("viewUpdatedArtifacts");
    }

    public void setViewUpdatedArtifacts(Integer viewUpdatedArtifacts) {
        set("viewUpdatedArtifacts", viewUpdatedArtifacts);
    }

    public Integer getViewCachedArtifacts() {
        return get("viewCachedArtifacts");
    }

    public void setViewCachedArtifacts(Integer viewCachedArtifacts) {
        set("viewCachedArtifacts", viewCachedArtifacts);
    }

    public Integer getViewDeployedArtifacts() {
        return get("viewDeployedArtifacts");
    }

    public void setViewDeployedArtifacts(Integer viewDeployedArtifacts) {
        set("viewDeployedArtifacts", viewDeployedArtifacts);
    }

    public Integer getViewSystemChanges() {
        return get("viewSystemChanges");
    }

    public void setViewSystemChanges(Integer viewSystemChanges) {
        set("viewSystemChanges", viewSystemChanges);
    }

    public Integer getMaintRepos() {
        return get("maintRepos");
    }

    public void setMaintRepos(Integer maintRepos) {
        set("maintRepos", maintRepos);
    }

    public Integer getMaintLogs() {
        return get("maintLogs");
    }

    public void setMaintLogs(Integer maintLogs) {
        set("maintLogs", maintLogs);
    }

    public Integer getMaintConfig() {
        return get("maintConfig");
    }

    public void setMaintConfig(Integer maintConfig) {
        set("maintConfig", maintConfig);
    }

    public Integer getConfigServer() {
        return get("configServer");
    }

    public void setConfigServer(Integer configServer) {
        set("configServer", configServer);
    }

    public Integer getConfigGroups() {
        return get("configGroups");
    }

    public void setConfigGroups(Integer configGroups) {
        set("configGroups", configGroups);
    }

    public Integer getConfigRules() {
        return get("configRules");
    }

    public void setConfigRules(Integer configRules) {
        set("configRules", configRules);
    }

    public Integer getConfigRepos() {
        return get("configRepos");
    }

    public void setConfigRepos(Integer configRepos) {
        set("configRepos", configRepos);
    }

}
