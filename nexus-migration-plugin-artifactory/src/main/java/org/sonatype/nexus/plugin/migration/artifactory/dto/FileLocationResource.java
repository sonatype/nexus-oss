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
package org.sonatype.nexus.plugin.migration.artifactory.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "fileLocation" )
public class FileLocationResource
{
    private String fileLocation;

    public String getFileLocation()
    {
        return fileLocation;
    }

    public void setFileLocation( String fileLocation )
    {
        this.fileLocation = fileLocation;
    }
}
