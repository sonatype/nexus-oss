package org.sonatype.nexus.rest.indextreeview;

import org.sonatype.nexus.index.treeview.DefaultMergedTreeNode;
import org.sonatype.nexus.index.treeview.IndexTreeView;
import org.sonatype.nexus.index.treeview.TreeNodeFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "indexBrowserTreeNode" )
public class IndexBrowserTreeNode
    extends DefaultMergedTreeNode
{
    private String classifier;
    private String extension;
    private String packaging;
    private String artifactUri;
    private String pomUri;    
    
    public IndexBrowserTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }
    
    public String getClassifier()
    {
        return classifier;
    }
    
    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }
    
    public String getExtension()
    {
        return extension;
    }
    
    public void setExtension( String extension )
    {
        this.extension = extension;
    }
    
    public String getArtifactUri()
    {
        return artifactUri;
    }
    
    public void setArtifactUri( String artifactUri )
    {
        this.artifactUri = artifactUri;
    }
    
    public String getPomUri()
    {
        return pomUri;
    }
    
    public void setPomUri( String pomUri )
    {
        this.pomUri = pomUri;
    }
    
    public String getPackaging()
    {
        return packaging;
    }
    
    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }
}
