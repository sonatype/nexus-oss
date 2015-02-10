/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.plugin.console.model;

/**
 * Plugin Information.
 */
@SuppressWarnings( "all" )
public class PluginInfo
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The name of the plugin.
     */
    private String name;

    /**
     * The version of the plugin.
     */
    private String version;

    /**
     * The description of the plugin.
     */
    private String description;

    /**
     * The status of the plugin.
     */
    private String status;

    /**
     * If a plugin is failed to be activated, the reason for it.
     */
    private String failureReason;

    /**
     * The scm last changed version of the plugin.
     */
    private String scmVersion;

    /**
     * The scm last changed timestamp of the plugin.
     */
    private String scmTimestamp;

    /**
     * The site of the plugin.
     */
    private String site;

    /**
     * Field documentation.
     */
    private java.util.List<DocumentationLink> documentation;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addDocumentation.
     * 
     * @param documentationLink
     */
    public void addDocumentation( DocumentationLink documentationLink )
    {
        getDocumentation().add( documentationLink );
    } //-- void addDocumentation( DocumentationLink )

    /**
     * Get the description of the plugin.
     * 
     * @return String
     */
    public String getDescription()
    {
        return this.description;
    } //-- String getDescription()

    /**
     * Method getDocumentation.
     * 
     * @return List
     */
    public java.util.List<DocumentationLink> getDocumentation()
    {
        if ( this.documentation == null )
        {
            this.documentation = new java.util.ArrayList<DocumentationLink>();
        }

        return this.documentation;
    } //-- java.util.List<DocumentationLink> getDocumentation()

    /**
     * Get if a plugin is failed to be activated, the reason for
     * it.
     * 
     * @return String
     */
    public String getFailureReason()
    {
        return this.failureReason;
    } //-- String getFailureReason()

    /**
     * Get the name of the plugin.
     * 
     * @return String
     */
    public String getName()
    {
        return this.name;
    } //-- String getName()

    /**
     * Get the scm last changed timestamp of the plugin.
     * 
     * @return String
     */
    public String getScmTimestamp()
    {
        return this.scmTimestamp;
    } //-- String getScmTimestamp()

    /**
     * Get the scm last changed version of the plugin.
     * 
     * @return String
     */
    public String getScmVersion()
    {
        return this.scmVersion;
    } //-- String getScmVersion()

    /**
     * Get the site of the plugin.
     * 
     * @return String
     */
    public String getSite()
    {
        return this.site;
    } //-- String getSite()

    /**
     * Get the status of the plugin.
     * 
     * @return String
     */
    public String getStatus()
    {
        return this.status;
    } //-- String getStatus()

    /**
     * Get the version of the plugin.
     * 
     * @return String
     */
    public String getVersion()
    {
        return this.version;
    } //-- String getVersion()

    /**
     * Method removeDocumentation.
     * 
     * @param documentationLink
     */
    public void removeDocumentation( DocumentationLink documentationLink )
    {
        getDocumentation().remove( documentationLink );
    } //-- void removeDocumentation( DocumentationLink )

    /**
     * Set the description of the plugin.
     * 
     * @param description
     */
    public void setDescription( String description )
    {
        this.description = description;
    } //-- void setDescription( String )

    /**
     * Set enunciate documentation.
     * 
     * @param documentation
     */
    public void setDocumentation( java.util.List<DocumentationLink> documentation )
    {
        this.documentation = documentation;
    } //-- void setDocumentation( java.util.List )

    /**
     * Set if a plugin is failed to be activated, the reason for
     * it.
     * 
     * @param failureReason
     */
    public void setFailureReason( String failureReason )
    {
        this.failureReason = failureReason;
    } //-- void setFailureReason( String )

    /**
     * Set the name of the plugin.
     * 
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    } //-- void setName( String )

    /**
     * Set the scm last changed timestamp of the plugin.
     * 
     * @param scmTimestamp
     */
    public void setScmTimestamp( String scmTimestamp )
    {
        this.scmTimestamp = scmTimestamp;
    } //-- void setScmTimestamp( String )

    /**
     * Set the scm last changed version of the plugin.
     * 
     * @param scmVersion
     */
    public void setScmVersion( String scmVersion )
    {
        this.scmVersion = scmVersion;
    } //-- void setScmVersion( String )

    /**
     * Set the site of the plugin.
     * 
     * @param site
     */
    public void setSite( String site )
    {
        this.site = site;
    } //-- void setSite( String )

    /**
     * Set the status of the plugin.
     * 
     * @param status
     */
    public void setStatus( String status )
    {
        this.status = status;
    } //-- void setStatus( String )

    /**
     * Set the version of the plugin.
     * 
     * @param version
     */
    public void setVersion( String version )
    {
        this.version = version;
    } //-- void setVersion( String )

}
