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
package org.sonatype.nexus.plugins.migration.util;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{

    public static XStream getXmlXStream()
    {
        XStream xs = org.sonatype.nexus.test.utils.XStreamFactory.getXmlXStream();

        xs.processAnnotations( MigrationSummaryDTO.class );
        xs.processAnnotations( MigrationSummaryResponseDTO.class );
        xs.processAnnotations( MigrationSummaryRequestDTO.class );
        xs.processAnnotations( RepositoryResolutionDTO.class );

        return xs;
    }

    public static XStream getJsonXStream()
    {
        XStream xs = org.sonatype.nexus.test.utils.XStreamFactory.getJsonXStream();

        xs.processAnnotations( MigrationSummaryDTO.class );
        xs.processAnnotations( MigrationSummaryResponseDTO.class );
        xs.processAnnotations( MigrationSummaryRequestDTO.class );
        xs.processAnnotations( RepositoryResolutionDTO.class );

        return xs;
    }

}
