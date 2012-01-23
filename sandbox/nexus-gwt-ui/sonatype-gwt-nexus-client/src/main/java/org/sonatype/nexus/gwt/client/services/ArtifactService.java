/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
