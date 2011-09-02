package org.sonatype.appcontext;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.sonatype.appcontext.source.LegacyBasedirEntrySource;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;
import org.sonatype.appcontext.source.SystemEnvironmentEntrySource;
import org.sonatype.appcontext.source.filter.FilteredEntrySource;
import org.sonatype.appcontext.source.filter.KeyEqualityEntryFilter;
import org.sonatype.appcontext.source.keys.DefaultSystemEnvironmentKeyTransformer;
import org.sonatype.appcontext.source.keys.KeyTransformingEntrySource;

public class SimpleTest
    extends TestCase
{
    public void testC01()
        throws Exception
    {
        // Set this to have it "catched"
        System.setProperty( "c01.blah", "tooMuchTalk!" );
        System.setProperty( "c01.basedir", new File( "src/test/resources/c01" ).getAbsolutePath() );
        System.setProperty( "plexus.bimbimbim", "yeah!" );

        AppContextRequest request = Factory.getDefaultRequest( "c01", null, Arrays.asList( "plexus" ) );
        request.getSources().add( new LegacyBasedirEntrySource( "c01.basedir", true ) );
        request.getSources().add(
            new PropertiesFileEntrySource( new File( "src/test/resources/c01/plexus.properties" ) ) );
        // this one applies "default" system env key transformation, hence $HOME key will become "home"
        request.getSources().add(
            new FilteredEntrySource( new KeyTransformingEntrySource( new SystemEnvironmentEntrySource(),
                new DefaultSystemEnvironmentKeyTransformer() ), new KeyEqualityEntryFilter( "home" ) ) );
        // this one those not applies "default" system env key transformation, hence $HOME ends up as "HOME"
        request.getSources().add(
            new FilteredEntrySource( new SystemEnvironmentEntrySource(), new KeyEqualityEntryFilter( "HOME" ) ) );

        AppContext appContext = Factory.create( request );

        assertEquals( 9, appContext.size() );

        // For reference, below is what should spit this out (naturally, paths would be different on different machine)
        // ===================================
        // Application context "c01" dump:
        // "c01.basedir"="/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01" (raw:
        // "/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01", src: legacyBasedir(key:"c01.basedir"))
        // "bimbimbim"="yeah!" (raw: "yeah!", src: prefixRemove(prefix:plexus., filter(keyStartsWith:[plexus.],
        // system(properties))))
        // "foo"="1" (raw: "1", src:
        // propsFile(/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01/plexus.properties, size:3))
        // "blah"="tooMuchTalk!" (raw: "tooMuchTalk!", src: prefixRemove(prefix:c01., filter(keyStartsWith:[c01.],
        // system(properties))))
        // "HOME"="/Users/cstamas" (raw: "/Users/cstamas", src: filter(keyIsIn:[HOME], system(env)))
        // "foointerpolated"="1" (raw: "${foo}", src:
        // propsFile(/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01/plexus.properties, size:3))
        // "home"="/Users/cstamas" (raw: "/Users/cstamas", src: filter(keyIsIn:[home],
        // defSysEnvTransformation(system(env))))
        // "bar"="2" (raw: "2", src:
        // propsFile(/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01/plexus.properties, size:3))
        // "basedir"="/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01" (raw:
        // "/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01", src: prefixRemove(prefix:c01.,
        // filter(keyStartsWith:[c01.], system(properties))))
        // Total of 9 entries.
        // ===================================

    }
}
