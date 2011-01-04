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
package org.sonatype.nexus.selenium.nexus2181;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.apache.maven.index.FlatSearchResponse;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ArtifactInformationPanel;
import org.sonatype.nexus.mock.pages.SearchTab;
import org.testng.annotations.Test;

@Component( role = Nexus2181SearchNotest.class )
public class Nexus2181SearchNotest
    // can't test plugins using selenium
    extends SeleniumTest
{

    @Requirement
    private PlexusContainer plexus;

    @Test
    public void quickSearch()
        throws Exception
    {
        FlatSearchResponse result =
            plexus.lookup( Searcher.class, "mavenCoordinates" ).flatSearch(
                Collections.singletonMap( "g", "nexus2181" ), "thirdparty", 0, Integer.MAX_VALUE, null );
        assertEquals( 2, result.getTotalHits() );

        SearchTab search = main.searchPanel().search( "nexus2181" );
        assertEquals( 2, search.getGrid().getStoreDataLength() );

        ArtifactInformationPanel artInfo = search.select( 0 );
        assertEquals( "nexus2181", artInfo.getGroupId().getValue() );
        assertEquals( "another-stuff", artInfo.getArtifactId().getValue() );
        assertEquals( "1.0.0", artInfo.getVersion().getValue() );

        artInfo = search.select( 1 );
        assertEquals( "nexus2181", artInfo.getGroupId().getValue() );
        assertEquals( "simple-artifact", artInfo.getArtifactId().getValue() );
        assertEquals( "1.0.0", artInfo.getVersion().getValue() );

        // pseudo refresh
        search.getAllSearch().type( "nexus2181" );
        search.getAllSearch().clickSearch();
        assertEquals( 2, search.getGrid().getStoreDataLength() );
        search.getAllSearch().clickClear();
        assertEquals( "null", search.getAllSearch().getValue() );
    }

    @Test
    public void searchKeyword()
    {
        SearchTab search = main.searchPanel().clickAdvancedSearch();
        assertEquals( 1, search.keywordSearch( "artifact" ).getGrid().getStoreDataLength() );
        assertEquals( 2, search.keywordSearch( "nexus2181" ).getGrid().getStoreDataLength() );
        assertEquals( 1, search.keywordSearch( "another" ).getGrid().getStoreDataLength() );
    }

    @Test
    public void searchClassname()
    {
        SearchTab search = main.searchPanel().clickAdvancedSearch();
        assertEquals( 1, search.classnameSearch( "org.apache.tools.ant.launch.AntMain" ).getGrid().getStoreDataLength() );
        assertEquals( 0,
            search.classnameSearch( "my.custom.unknow.class.to.not.be.found.AntMain" ).getGrid().getStoreDataLength() );
    }

    @Test
    public void searchChecksum()
    {
        SearchTab search = main.searchPanel().clickAdvancedSearch();
        assertEquals( 0, search.checksumSearch( "12345" ).getGrid().getStoreDataLength() );
    }

}
