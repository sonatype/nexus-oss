/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm58;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nxcm58NexusCommonUseXmlIT
    extends Nxcm58NexusCommonUseJsonIT
{

    public Nxcm58NexusCommonUseXmlIT()
    {
        super();
        this.xstream = this.getXMLXStream();
        this.mediaType = MediaType.APPLICATION_XML;
    }

}
