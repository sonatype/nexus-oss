#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VirusScannerTest
{

    private VirusScanner scanner;

    @BeforeMethod
    public void createScanner()
        throws Exception
    {
        scanner = new XYVirusScanner(); // TODO must lookup for it!
    }

    @Test
    public void scan()
    {
        StorageFileItem file = mock( StorageFileItem.class );
        when( file.getName() ).thenReturn( "clean-file.xtx" );
        Assert.assertFalse( scanner.hasVirus( file ) );
        when( file.getName() ).thenReturn( "infected-file.xtx" );
        Assert.assertTrue( scanner.hasVirus( file ) );
    }

}
