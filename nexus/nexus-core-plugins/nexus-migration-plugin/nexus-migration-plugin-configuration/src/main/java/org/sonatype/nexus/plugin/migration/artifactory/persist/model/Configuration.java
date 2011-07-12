/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.persist.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( "mappingConfiguration" )
public class Configuration
{

    private String nexusContext;

    @XStreamImplicit
    private List<CMapping> urlsMapping;

    public List<CMapping> getUrlsMapping()
    {
        if ( urlsMapping == null )
        {
            urlsMapping = new ArrayList<CMapping>();
        }
        return urlsMapping;
    }

    public void setUrlsMapping( List<CMapping> maps )
    {
        this.urlsMapping = maps;
    }

    public void addUrlMapping( CMapping map )
    {
        getUrlsMapping().add( map );
    }

    public String getNexusContext()
    {
        return nexusContext;
    }

    public void setNexusContext( String nexusContext )
    {
        this.nexusContext = nexusContext;
    }

}
