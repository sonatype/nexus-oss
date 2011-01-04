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
package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class ArtifactInformationPanel
    extends Component
{

    private TextField groupId;

    private TextField artifactId;

    private TextField version;

    public ArtifactInformationPanel( Selenium selenium, String expression )
    {
        super( selenium, expression );

        groupId = new TextField( selenium, expression + ".find('name', 'groupId')[0]" );
        artifactId = new TextField( selenium, expression + ".find('name', 'artifactId')[0]" );
        version = new TextField( selenium, expression + ".find('name', 'version')[0]" );
    }

    public TextField getGroupId()
    {
        return groupId;
    }

    public TextField getArtifactId()
    {
        return artifactId;
    }

    public TextField getVersion()
    {
        return version;
    }

}
