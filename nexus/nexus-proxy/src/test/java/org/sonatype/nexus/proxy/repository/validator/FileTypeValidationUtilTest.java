package org.sonatype.nexus.proxy.repository.validator;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;

public class FileTypeValidationUtilTest
    extends AbstractNexusTestCase
{
    private FileTypeValidatorHub getValidationUtil()
        throws Exception
    {
        return lookup( FileTypeValidatorHub.class );
    }

    public void testJar()
        throws Exception
    {
        doTest( "something/else/myapp.jar", "test.jar", true );
        doTest( "something/else/myapp.zip", "test.jar", true );
        doTest( "something/else/myapp.war", "test.jar", true );
        doTest( "something/else/myapp.ear", "test.jar", true );
        doTest( "something/else/myapp.jar", "error.html", false );
    }

    public void testPom()
        throws Exception
    {
        doTest( "something/else/myapp.pom", "no-doctype-pom.xml", true );
        doTest( "something/else/myapp.pom", "simple.xml", false );
        doTest( "something/else/myapp.pom", "pom.xml", true );
        doTest( "something/else/myapp.xml", "pom.xml", true );
        doTest( "something/else/myapp.xml", "simple.xml", true );
        doTest( "something/else/myapp.xml", "error.html", false );
    }

    public void testNonHandled()
        throws Exception
    {
        doTest( "something/else/image.jpg", "no-doctype-pom.xml", true );
        doTest( "something/else/image.avi", "no-doctype-pom.xml", true );
    }

    // ==

    protected Repository getDummyRepository()
        throws Exception
    {
        Repository repository = lookup( Repository.class, "maven2" );

        CRepository cRepo = new DefaultCRepository();
        cRepo.setId( "test-repo" );
        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString() );
        cRepo.setNotFoundCacheTTL( 1 );
        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setProvider( "file" );
        cRepo.setProviderRole( Repository.class.getName() );
        cRepo.setProviderHint( "maven2" );

        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        cRepo.setExternalConfiguration( ex );
        M2RepositoryConfiguration extConf = new M2RepositoryConfiguration( ex );
        extConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        repository.configure( cRepo );

        return repository;
    }

    private void doTest( String expectedFileName, String testFileName, boolean expectedResult )
        throws Exception
    {
        File testFile = new File( "target/test-classes/FileTypeValidationUtilTest", testFileName );

        DefaultStorageFileItem file =
            new DefaultStorageFileItem( getDummyRepository(), new ResourceStoreRequest( expectedFileName ), true, true,
                new FileContentLocator( testFile, "this-is-neglected-in-this-test" ) );

        boolean result = getValidationUtil().isExpectedFileType( file );

        Assert.assertEquals( "File name: " + expectedFileName + " and file: " + testFileName + " match result: "
            + result + " expected: " + expectedResult, expectedResult, result );
    }

    //

}
