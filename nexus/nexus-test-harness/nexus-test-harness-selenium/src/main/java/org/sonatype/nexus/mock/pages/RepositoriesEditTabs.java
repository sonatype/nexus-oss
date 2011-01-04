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

import com.thoughtworks.selenium.Selenium;

public class RepositoriesEditTabs
    extends Component
{

    public enum RepoKind
    {
        HOSTED( 2, 4, 3 ), PROXY( 2, 4, 3 ), VIRTUAL( 1, 2, -1 ), GROUP( 2, -1, -1 );

        private int configPosition;

        private int summaryPosition;

        private int mirrorPosition;

        private RepoKind( int configPosition, int summaryPosition, int mirrorPosition )
        {
            this.configPosition = configPosition;
            this.summaryPosition = summaryPosition;
            this.mirrorPosition = mirrorPosition;
        }
    }

    private RepoKind kind;

    public RepositoriesEditTabs( Selenium selenium, RepoKind kind )
    {
        super( selenium, RepositoriesTab.REPOSITORIES_ST + ".cardPanel.getLayout().activeItem.tabPanel" );
        this.kind = kind;
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public Component selectConfiguration()
    {
        select( kind.configPosition );

        if ( RepoKind.GROUP.equals( kind ) )
        {
            return new GroupConfigurationForm( selenium, expression + ".getLayout().activeItem" );
        }
        else
        {
            return new RepositoriesConfigurationForm( selenium, expression + ".getLayout().activeItem" );
        }
    }

    public RepositoriesArtifactUploadForm selectUpload()
    {
        if ( !RepoKind.HOSTED.equals( kind ) )
        {
            return null;
        }

        select( 5 );

        return new RepositoriesArtifactUploadForm( selenium, expression + ".getLayout().activeItem" );
    }

    public RepositorySummary selectSummary()
    {
        if ( RepoKind.GROUP.equals( kind ) )
        {
            return null;
        }

        select( kind.summaryPosition );

        return new RepositorySummary( selenium, expression + ".getLayout().activeItem" );
    }

    public RepositoryMirror selectMirror()
    {
        if ( !( RepoKind.PROXY.equals( kind ) || RepoKind.HOSTED.equals( kind ) ) )
        {
            return null;
        }

        select( kind.mirrorPosition );

        return new RepositoryMirror( selenium, expression + ".getLayout().activeItem" );
    }
}
