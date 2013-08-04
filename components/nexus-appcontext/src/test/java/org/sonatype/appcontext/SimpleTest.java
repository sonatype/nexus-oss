/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.appcontext;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.sonatype.appcontext.internal.ContextStringDumper;
import org.sonatype.appcontext.source.LegacyBasedirEntrySource;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;
import org.sonatype.appcontext.source.SystemEnvironmentEntrySource;
import org.sonatype.appcontext.source.filter.FilteredEntrySource;
import org.sonatype.appcontext.source.filter.KeyEqualityEntryFilter;
import org.sonatype.appcontext.source.keys.KeyTransformingEntrySource;
import org.sonatype.appcontext.source.keys.LegacySystemEnvironmentKeyTransformer;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SimpleTest
    extends TestCase
{
  public void testC01()
      throws Exception
  {
    // Set this to have it "catched"
    System.setProperty("c01.blah", "tooMuchTalk!");
    System.setProperty("c01.blah-blah", "dash");
    System.setProperty("c01.blah.blah", "dot");
    System.setProperty("c01.basedir", new File("src/test/resources/c01").getAbsolutePath());
    System.setProperty("plexus.bimbimbim", "yeah!");
    System.setProperty("plexus.bimbimbim-dash", "dash");
    System.setProperty("plexus.bimbimbim.dot", "dot");
    System.setProperty("included", "I am included!");

    // ctx ID is "c01", but has alias "plexus" too. Will gather those set above from system props
    AppContextRequest request = Factory.getDefaultRequest("c01", null, Arrays.asList("plexus"), "included");

    // +1 the basedir
    request.getSources().add(new LegacyBasedirEntrySource("c01.basedir", true));

    // +3 from properties file
    request.getSources().add(
        new PropertiesFileEntrySource(new File("src/test/resources/c01/plexus.properties")));

    // +1 from env: this one applies "default" system env key transformation, hence $HOME key will become "home"
    request.getSources().add(
        new FilteredEntrySource(new KeyTransformingEntrySource(new SystemEnvironmentEntrySource(),
            new LegacySystemEnvironmentKeyTransformer()), new KeyEqualityEntryFilter("home")));

    // +1 from env: this one those not applies "default" system env key transformation, hence $HOME ends up as
    // "HOME"
    request.getSources().add(
        new FilteredEntrySource(new SystemEnvironmentEntrySource(), new KeyEqualityEntryFilter("HOME")));

    AppContext appContext = Factory.create(request);

    assertEquals(14, appContext.size());

    // For reference, below is what should spit this out (naturally, paths would be different on different machine)
    // ===================================
    // Application context "c01" dump:
    // "bimbimbim.dot"="dot" (raw: "dot", src: prefixRemove(prefix:plexus., filter(keyStartsWith:[plexus.],
    // system(properties))))
    // "c01.basedir"="/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01" (raw:
    // "/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01", src: legacyBasedir(key:"c01.basedir"))
    // "blah-blah"="dash" (raw: "dash", src: prefixRemove(prefix:c01., filter(keyStartsWith:[c01.],
    // system(properties))))
    // "blah.blah"="dot" (raw: "dot", src: prefixRemove(prefix:c01., filter(keyStartsWith:[c01.],
    // system(properties))))
    // "bimbimbim"="yeah!" (raw: "yeah!", src: prefixRemove(prefix:plexus., filter(keyStartsWith:[plexus.],
    // system(properties))))
    // "foo"="1" (raw: "1", src:
    // propsFile(/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01/plexus.properties, size:3))
    // "blah"="tooMuchTalk!" (raw: "tooMuchTalk!", src: prefixRemove(prefix:c01., filter(keyStartsWith:[c01.],
    // system(properties))))
    // "HOME"="/Users/cstamas" (raw: "/Users/cstamas", src: filter(keyIsIn:[HOME], system(env)))
    // "included"="I am included!" (raw: "I am included!", src: filter(keyIsIn:[included], system(properties)))
    // "foointerpolated"="1" (raw: "${foo}", src:
    // propsFile(/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01/plexus.properties, size:3))
    // "bimbimbim-dash"="dash" (raw: "dash", src: prefixRemove(prefix:plexus., filter(keyStartsWith:[plexus.],
    // system(properties))))
    // "home"="/Users/cstamas" (raw: "/Users/cstamas", src: filter(keyIsIn:[home],
    // defSysEnvTransformation(system(env))))
    // "bar"="2" (raw: "2", src:
    // propsFile(/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01/plexus.properties, size:3))
    // "basedir"="/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01" (raw:
    // "/Users/cstamas/worx/sonatype/appcontext/src/test/resources/c01", src: prefixRemove(prefix:c01.,
    // filter(keyStartsWith:[c01.], system(properties))))
    // Total of 13 entries.
    // ===================================
  }

  public void testTimestamps()
      throws Exception
  {
    // Set this to have it "catched"
    System.setProperty("c01.blah", "tooMuchTalk!");
    System.setProperty("c01.blah-blah", "dash");
    System.setProperty("c01.blah.blah", "dot");
    System.setProperty("c01.basedir", new File("src/test/resources/c01").getAbsolutePath());
    System.setProperty("plexus.bimbimbim", "yeah!");
    System.setProperty("plexus.bimbimbim-dash", "dash");
    System.setProperty("plexus.bimbimbim.dot", "dot");
    System.setProperty("included", "I am included!");

    // ctx ID is "c01", but has alias "plexus" too. Will gather those set above from system props
    AppContextRequest request = Factory.getDefaultRequest("c01", null, Arrays.asList("plexus"), "included");

    // +1 the basedir
    request.getSources().add(new LegacyBasedirEntrySource("c01.basedir", true));

    // +3 from properties file
    request.getSources().add(
        new PropertiesFileEntrySource(new File("src/test/resources/c01/plexus.properties")));

    // +1 from env: this one applies "default" system env key transformation, hence $HOME key will become "home"
    request.getSources().add(
        new FilteredEntrySource(new KeyTransformingEntrySource(new SystemEnvironmentEntrySource(),
            new LegacySystemEnvironmentKeyTransformer()), new KeyEqualityEntryFilter("home")));

    // +1 from env: this one those not applies "default" system env key transformation, hence $HOME ends up as
    // "HOME"
    request.getSources().add(
        new FilteredEntrySource(new SystemEnvironmentEntrySource(), new KeyEqualityEntryFilter("HOME")));

    AppContext appContext = Factory.create(request);

    assertEquals(14, appContext.size());

    final long created = appContext.getCreated();
    long modified = appContext.getModified();
    Thread.sleep(5); // resolution is millis, so to be sure we have diff on fast HW

    Assert.assertEquals(created, modified);

    appContext.remove("included");
    Assert.assertEquals(13, appContext.size());

    Assert.assertTrue(modified < appContext.getModified());
    modified = appContext.getModified();
    Thread.sleep(5); // resolution is millis, so to be sure we have diff on fast HW

    appContext.put("foo1", "bar1");
    Assert.assertEquals(14, appContext.size());

    Assert.assertTrue(modified < appContext.getModified());
    modified = appContext.getModified();
    Thread.sleep(5); // resolution is millis, so to be sure we have diff on fast HW

    appContext.clear();
    Assert.assertEquals(0, appContext.size());

    Assert.assertTrue(modified < appContext.getModified());
    modified = appContext.getModified();
    Thread.sleep(5); // resolution is millis, so to be sure we have diff on fast HW
  }

  public void testFromMap() {
    final HashMap<String, Object> aMap = new HashMap<String, Object>();
    aMap.put("foo", "fooValue");
    aMap.put("bar", "barValue");
    final AppContext appContext = Factory.create("test", null, aMap);
    assertEquals(2, appContext.size());
    System.out.println(ContextStringDumper.dumpToString(appContext));
  }
}
