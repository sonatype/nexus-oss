package org.sonatype.nexus.selenium.nexus2237;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepoTargetForm;
import org.sonatype.nexus.mock.pages.RepoTargetTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;

public class Nexus2237RepoTargetTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RepoTargetForm target = main.openRepoTargets().addRepoTarget();

        NxAssert.requiredField( target.getName(), "target" );
        NxAssert.requiredField( target.getRepositoryType(), "maven2" );

        target.save();
        NxAssert.hasErrorText( target.getPattern(), "The target should have at least one pattern." );

        target.addPattern( ".*" );
        NxAssert.noErrorText( target.getPattern() );

        target.cancel();
    }

    @Test
    public void repoTargetCRUD()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RepoTargetTab targets = main.openRepoTargets();

        // create
        String name = "seleniumTarget";
        String repoType = "maven2";
        final String pattern = ".*";
        final String pattern2 = ".*maven-metadata\\.xml.*";

        MockListener ml = MockHelper.listen( "/repo_targets", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
                Assert.assertThat( payload, not( nullValue() ) );
                RepositoryTargetResourceResponse result = (RepositoryTargetResourceResponse) payload;

                Assert.assertThat( result.getData().getPatterns(), IsCollectionContaining.hasItems( pattern, pattern2 ) );
            }
        } );

        targets.addRepoTarget().populate( name, repoType, pattern, pattern2 ).save();

        RepositoryTargetResourceResponse result = (RepositoryTargetResourceResponse) ml.getResult();
        String targetId = nexusBaseURL + "service/local/repo_targets/" + result.getData().getId();

        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        targets.refresh();

        Assert.assertTrue( targets.getGrid().contains( targetId ) );
        targets.refresh();

        // read
        RepoTargetForm target = targets.select( targetId );
        NxAssert.valueEqualsTo( target.getName(), name );
        NxAssert.valueEqualsTo( target.getRepositoryType(), repoType );

        targets.refresh();

        // update
        String newName = "new selenium repository target";

        target = targets.select( targetId );
        target.getName().type( newName );
        target.save();

        targets.refresh();
        target = targets.select( targetId );
        NxAssert.valueEqualsTo( target.getName(), newName );

        targets.refresh();

        // delete
        targets.select( targetId );
        targets.delete().clickYes();
        targets.refresh();

        Assert.assertFalse( targets.getGrid().contains( targetId ) );
    }
}
