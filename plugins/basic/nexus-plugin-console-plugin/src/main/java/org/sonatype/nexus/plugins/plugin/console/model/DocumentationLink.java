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
 * REST resource information.
 */
@SuppressWarnings( "all" )
public class DocumentationLink
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The link label.
     */
    private String label;

    /**
     * The link URL.
     */
    private String url;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the link label.
     * 
     * @return String
     */
    public String getLabel()
    {
        return this.label;
    } //-- String getLabel()

    /**
     * Get the link URL.
     * 
     * @return String
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl()

    /**
     * Set the link label.
     * 
     * @param label
     */
    public void setLabel( String label )
    {
        this.label = label;
    } //-- void setLabel( String )

    /**
     * Set the link URL.
     * 
     * @param url
     */
    public void setUrl( String url )
    {
        this.url = url;
    } //-- void setUrl( String )

}
