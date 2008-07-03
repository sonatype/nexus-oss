package org.sonatype.nexus.gwt.client.services;

import org.sonatype.gwt.client.handler.EntityResponseHandler;

/**
 * This service publish various informations about the POM. The trick is here that we follow the "sections" described on
 * http://maven.apache.org/pom.html to make users more easily recognize and be familiar with them.
 * 
 * @author cstamas
 */
public interface ArtifactService
{
    void getArtifact( String gid, String aid, String version, EntityResponseHandler handler );

    void getArtifactBasicInfos( String gid, String aid, String version, EntityResponseHandler handler );

    void getArtifactBuildInfos( String gid, String aid, String version, EntityResponseHandler handler );

    void getArtifactProjectInfos( String gid, String aid, String version, EntityResponseHandler handler );

    void getArtifactEnvironmentInfos( String gid, String aid, String version, EntityResponseHandler handler );
}
