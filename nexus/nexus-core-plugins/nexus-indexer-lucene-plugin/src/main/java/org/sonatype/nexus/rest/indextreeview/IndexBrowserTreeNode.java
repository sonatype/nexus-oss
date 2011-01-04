/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.indextreeview;

import org.apache.maven.index.treeview.IndexTreeView;
import org.apache.maven.index.treeview.TreeViewRequest;
import org.sonatype.nexus.index.treeview.DefaultMergedTreeNode;

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
    public IndexBrowserTreeNode( IndexTreeView tview, TreeViewRequest request )
    {
        super( tview, request );
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
