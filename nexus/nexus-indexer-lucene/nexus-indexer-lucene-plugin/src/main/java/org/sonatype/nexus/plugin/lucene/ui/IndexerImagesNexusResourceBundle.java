package org.sonatype.nexus.plugin.lucene.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.mime.MimeUtil;
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
    private MimeUtil mimeUtil;

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
            this.mimeUtil.getMimeType( path ) );
    }

}
