/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
