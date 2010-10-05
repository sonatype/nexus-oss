/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.plugin.lucene.security;

import javax.inject.Named;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Named( "IndexerLuceneSecurityResource" )
public class IndexerLuceneSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource
{       
    @Override
    public String getResourcePath()
    {
        return "/META-INF/nexus-indexer-lucene-static-security.xml";
    }
}
