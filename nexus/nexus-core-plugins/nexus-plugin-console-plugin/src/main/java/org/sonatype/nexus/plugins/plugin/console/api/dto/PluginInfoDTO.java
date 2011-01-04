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
package org.sonatype.nexus.plugins.plugin.console.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "pluginInfo" )
@XmlType( name = "pluginInfo" )
public class PluginInfoDTO
{
    private String name;

    private String description;

    private String version;

    private String status;

    private String failureReason;

    private String scmVersion;

    private String scmTimestamp;

    private String site;

    private List<DocumentationLinkDTO> documentation;

    private List<RestInfoDTO> restInfos = new ArrayList<RestInfoDTO>();

    public String getSite()
    {
        return site;
    }

    public void setSite( String site )
    {
        this.site = site;
    }

    public String getFailureReason()
    {
        return failureReason;
    }

    public void setFailureReason( String failureReason )
    {
        this.failureReason = failureReason;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getScmVersion()
    {
        return scmVersion;
    }

    public void setScmVersion( String scmVersion )
    {
        this.scmVersion = scmVersion;
    }

    public String getScmTimestamp()
    {
        return scmTimestamp;
    }

    public void setScmTimestamp( String scmTimestamp )
    {
        this.scmTimestamp = scmTimestamp;
    }

    @XmlElementWrapper( name = "restInfos" )
    @XmlElement( name = "restInfo" )
    public List<RestInfoDTO> getRestInfos()
    {
        return restInfos;
    }

    public void setRestInfos( List<RestInfoDTO> restInfos )
    {
        this.restInfos = restInfos;
    }

    public void addRestInfo( RestInfoDTO restInfo )
    {
        this.restInfos.add( restInfo );
    }

    public List<DocumentationLinkDTO> getDocumentation()
    {
        return documentation;
    }

    public void setDocumentation( List<DocumentationLinkDTO> documentation )
    {
        this.documentation = documentation;
    }

}
