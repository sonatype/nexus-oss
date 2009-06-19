package org.sonatype.nexus.selenium.nexus2181;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ArtifactInformationPanel;
import org.sonatype.nexus.mock.pages.SearchTab;

public class Nexus2181SearchTest
    extends SeleniumTest
{

    @Test
    public void quickSearch()
    {
        SearchTab search = main.searchPanel().search( "nexus2181" );
        Assert.assertEquals( 2, search.getGrid().getStoreDataLength() );

        ArtifactInformationPanel artInfo = search.select( 0 );
        Assert.assertEquals( "nexus2181", artInfo.getGroupId().getValue() );
        Assert.assertEquals( "another-stuff", artInfo.getArtifactId().getValue() );
        Assert.assertEquals( "1.0.0", artInfo.getVersion().getValue() );

        artInfo = search.select( 1 );
        Assert.assertEquals( "nexus2181", artInfo.getGroupId().getValue() );
        Assert.assertEquals( "simple-artifact", artInfo.getArtifactId().getValue() );
        Assert.assertEquals( "1.0.0", artInfo.getVersion().getValue() );

        // pseudo refresh
        search.getAllSearch().type( "nexus2181" );
        search.getAllSearch().clickSearch();
        Assert.assertEquals( 2, search.getGrid().getStoreDataLength() );
        search.getAllSearch().clickClear();
        Assert.assertEquals( "null", search.getAllSearch().getValue() );
    }

    @Test
    public void searchKeyword()
    {
        SearchTab search = main.searchPanel().clickAdvancedSearch();
        Assert.assertEquals( 1, search.keywordSearch( "artifact" ).getGrid().getStoreDataLength() );
        Assert.assertEquals( 2, search.keywordSearch( "nexus2181" ).getGrid().getStoreDataLength() );
        Assert.assertEquals( 1, search.keywordSearch( "another" ).getGrid().getStoreDataLength() );
    }

    @Test
    public void searchClassname()
    {
        SearchTab search = main.searchPanel().clickAdvancedSearch();
        Assert.assertEquals(
                             1,
                             search.classnameSearch( "org.apache.tools.ant.launch.AntMain" ).getGrid().getStoreDataLength() );
        Assert.assertEquals(
                             0,
                             search.classnameSearch( "my.custom.unknow.class.to.not.be.found.AntMain" ).getGrid().getStoreDataLength() );
    }

    @Test
    public void searchChecksum()
    {
        SearchTab search = main.searchPanel().clickAdvancedSearch();
        Assert.assertEquals( 0, search.checksumSearch( "12345" ).getGrid().getStoreDataLength() );
    }

}
