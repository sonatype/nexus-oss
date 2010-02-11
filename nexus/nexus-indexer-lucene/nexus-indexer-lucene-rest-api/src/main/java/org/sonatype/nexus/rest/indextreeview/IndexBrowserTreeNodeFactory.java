package org.sonatype.nexus.rest.indextreeview;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.DefaultMergedTreeNodeFactory;
import org.sonatype.nexus.index.treeview.IndexTreeView;
import org.sonatype.nexus.index.treeview.TreeNode;
import org.sonatype.nexus.proxy.repository.Repository;

public class IndexBrowserTreeNodeFactory
    extends DefaultMergedTreeNodeFactory
{
    private String baseLinkUrl;
    
    public IndexBrowserTreeNodeFactory( IndexingContext ctx, Repository repository, String baseLinkUrl )
    {
        super( ctx, repository );
        this.baseLinkUrl = baseLinkUrl;
    }

    @Override
    protected TreeNode decorateArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path, TreeNode node )
    {
        IndexBrowserTreeNode iNode = ( IndexBrowserTreeNode ) super.decorateArtifactNode( tview, ai, path, node );
        
        iNode.setClassifier( ai.classifier );
        iNode.setExtension( ai.fextension );
        iNode.setPackaging( ai.packaging );
        iNode.setArtifactUri( buildArtifactUri( iNode ) );
        iNode.setPomUri( buildPomUri( iNode ) );

        return iNode;
    }

    @Override
    protected TreeNode instantiateNode( IndexTreeView tview, String path, boolean leaf, String nodeName )
    {
        return new IndexBrowserTreeNode( tview, this );
    }
    
    protected String buildArtifactUri( IndexBrowserTreeNode node )
    {
        if ( StringUtils.isEmpty( node.getPackaging() ) 
            || "pom".equals( node.getPackaging() ) )
        {
            return "";
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append( "?r=" );
        sb.append( node.getRepositoryId() );
        sb.append( "&g=" );
        sb.append( node.getGroupId() );
        sb.append( "&a=" );
        sb.append( node.getArtifactId() );
        sb.append( "&v=" );
        sb.append( node.getVersion() );
        sb.append( "&p=" );
        sb.append(  node.getPackaging() );

        return this.baseLinkUrl + sb.toString();
    }
    
    protected String buildPomUri( IndexBrowserTreeNode node )
    {
        if ( StringUtils.isNotEmpty( node.getClassifier() ) )
        {
            return "";
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append( "?r=" );
        sb.append( node.getRepositoryId() );
        sb.append( "&g=" );
        sb.append( node.getGroupId() );
        sb.append( "&a=" );
        sb.append( node.getArtifactId() );
        sb.append( "&v=" );
        sb.append( node.getVersion() );
        sb.append( "&p=pom" );

        return this.baseLinkUrl + sb.toString();
    }
}
