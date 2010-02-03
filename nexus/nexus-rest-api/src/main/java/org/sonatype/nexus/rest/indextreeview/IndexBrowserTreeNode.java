package org.sonatype.nexus.rest.indextreeview;

import org.sonatype.nexus.index.treeview.DefaultMergedTreeNode;
import org.sonatype.nexus.index.treeview.IndexTreeView;
import org.sonatype.nexus.index.treeview.TreeNodeFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Adds some more details to the TreeNode, some items that are required for index browsing in the UI
 */
@XStreamAlias( "indexBrowserTreeNode" )
public class IndexBrowserTreeNode
    extends DefaultMergedTreeNode
{
    /**
     * The classifier of the artifact.
     */
    private String classifier;
    
    /**
     * The file extension of the artifact.
     */
    private String extension;
    
    /**
     * The packaging of the artifact.
     */
    private String packaging;
    
    /**
     * The URI of the artifact.
     */
    private String artifactUri;
    
    /**
     * The URI of the artifact's pom file.
     */
    private String pomUri;    
    
    /**
     * Constructor that takes an IndexTreeView implmentation and a TreeNodeFactory implementation;
     * 
     * @param tview
     * @param factory
     */
    public IndexBrowserTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }
    
    /**
     * Get the classifier of the artifact.
     * 
     * @return String
     */
    public String getClassifier()
    {
        return classifier;
    }
    
    /**
     * Set the classifier of the artifact.
     * 
     * @param String
     */
    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }
    
    /**
     * Get the file extension of the artifact.
     * 
     * @return String
     */
    public String getExtension()
    {
        return extension;
    }
    
    /**
     * Set the file extension of the artifact.
     * 
     * @param String
     */
    public void setExtension( String extension )
    {
        this.extension = extension;
    }
    
    /**
     * Get the URI of the artifact.
     * 
     * @return String
     */
    public String getArtifactUri()
    {
        return artifactUri;
    }
    
    /**
     * Set the URI of the artifact.
     * 
     * @param String
     */
    public void setArtifactUri( String artifactUri )
    {
        this.artifactUri = artifactUri;
    }
    
    /**
     * Get the URI of the artifact's pom file.
     * 
     * @return String
     */
    public String getPomUri()
    {
        return pomUri;
    }
    
    /**
     * Set the URI of the artifact's pom file.
     * 
     * @param String
     */
    public void setPomUri( String pomUri )
    {
        this.pomUri = pomUri;
    }
    
    /**
     * Get the packaging of the artifact.
     * 
     * @return String
     */
    public String getPackaging()
    {
        return packaging;
    }
    
    /**
     * Set the packaging of the artifact.
     * 
     * @param String
     */
    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }
}
