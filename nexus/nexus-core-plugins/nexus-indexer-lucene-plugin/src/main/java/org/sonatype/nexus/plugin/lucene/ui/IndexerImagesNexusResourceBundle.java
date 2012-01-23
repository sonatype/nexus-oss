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
package org.sonatype.nexus.plugin.lucene.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Named( "IndexerImagesNexusResourceBundle" )
public class IndexerImagesNexusResourceBundle
    extends AbstractNexusResourceBundle
    implements NexusResourceBundle
{

    @Inject
    private MimeSupport mimeSupport;

    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        result.add( newStaticResource( "/css/indexer-lucene-plugin.css") );

        return result;
    }

    private StaticResource newStaticResource( String path )
    {
        return new DefaultStaticResource( getClass().getResource( "/static" + path ), path,
            mimeSupport.guessMimeTypeFromPath( path ));
    }

}
