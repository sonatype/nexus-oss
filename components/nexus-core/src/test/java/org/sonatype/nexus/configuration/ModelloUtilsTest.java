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

package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Arrays;

import org.sonatype.nexus.configuration.ModelloUtils.ModelReader;
import org.sonatype.nexus.configuration.ModelloUtils.ModelUpgrader;
import org.sonatype.nexus.configuration.ModelloUtils.ModelWriter;
import org.sonatype.nexus.util.file.FileSupport;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

/**
 * UT for {@link ModelloUtils}.
 *
 * @since 2.7.0
 */
public class ModelloUtilsTest
    extends TestSupport
{
  public static final ModelReader<Xpp3Dom> DOM_READER = new ModelReader<Xpp3Dom>()
  {
    @Override
    public Xpp3Dom read(final Reader reader) throws IOException, XmlPullParserException {
      return Xpp3DomBuilder.build(reader);
    }
  };

  public static final ModelWriter<Xpp3Dom> DOM_WRITER = new ModelWriter<Xpp3Dom>()
  {
    @Override
    public void write(final Writer writer, final Xpp3Dom model) throws IOException {
      Xpp3DomWriter.write(writer, model);
      writer.flush();
    }
  };

  public static final ModelUpgrader V1_V2_UPGRADER = new ModelUpgrader()
  {
    @Override
    public String fromVersion() {
      return "1";
    }

    @Override
    public String toVersion() {
      return "2";
    }

    @Override
    public void upgrade(final Reader reader, final Writer writer) throws IOException, XmlPullParserException {
      final Xpp3Dom dom = DOM_READER.read(reader);
      dom.getChild("version").setValue(toVersion());
      final Xpp3Dom newnode = new Xpp3Dom("v2field");
      newnode.setValue("foo");
      dom.addChild(newnode);
      DOM_WRITER.write(writer, dom);
    }
  };

  public static final ModelUpgrader V2_V3_UPGRADER = new ModelUpgrader()
  {
    @Override
    public String fromVersion() {
      return "2";
    }

    @Override
    public String toVersion() {
      return "3";
    }

    @Override
    public void upgrade(final Reader reader, final Writer writer) throws IOException, XmlPullParserException {
      final Xpp3Dom dom = DOM_READER.read(reader);
      dom.getChild("version").setValue(toVersion());
      final Xpp3Dom newnode = new Xpp3Dom("v3field");
      newnode.setValue("bar");
      dom.addChild(newnode);
      DOM_WRITER.write(writer, dom);
    }
  };

  public static final ModelUpgrader V2_V3_UPGRADER_FAILING = new ModelUpgrader()
  {
    @Override
    public String fromVersion() {
      return "2";
    }

    @Override
    public String toVersion() {
      return "3";
    }

    @Override
    public void upgrade(final Reader reader, final Writer writer) throws IOException, XmlPullParserException {
      throw new XmlPullParserException(getClass().getSimpleName());
    }
  };

  @Test
  public void plainUpgrade() throws Exception {
    final String payload = "<foo><version>1</version></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    final Xpp3Dom dom = ModelloUtils.load("3", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));

    assertThat(dom.toString(), equalTo(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<foo>\n  <version>3</version>\n  <v2field>foo</v2field>\n  <v3field>bar</v3field>\n</foo>"));
  }

  @Test
  public void intermediateUpgrade() throws Exception {
    final String payload = "<foo><version>1</version></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    final Xpp3Dom dom = ModelloUtils.load("2", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));

    assertThat(dom.toString(), equalTo(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<foo>\n  <version>2</version>\n  <v2field>foo</v2field>\n</foo>"));
  }

  @Test
  public void intermediateNoUpgrade() throws Exception {
    final String payload = "<foo><version>1</version></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    final Xpp3Dom dom = ModelloUtils.load("1", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));

    assertThat(dom.toString(), equalTo(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<foo>\n  <version>1</version>\n</foo>"));
  }

  @Test(expected = XmlPullParserException.class)
  public void noVersionNode() throws Exception {
    final String payload = "<foo></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    ModelloUtils.load("1", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));
  }

  @Test(expected = XmlPullParserException.class)
  public void emptyVersionNode() throws Exception {
    final String payload = "<foo><version/></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    ModelloUtils.load("1", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));
  }

  @Test(expected = XmlPullParserException.class)
  public void corruptXmlModel() throws Exception {
    final String payload = "<foo version/></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    ModelloUtils.load("1", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));
  }

  @Test(expected = XmlPullParserException.class)
  public void corruptXmlModelDuringUpgrade() throws Exception {
    final String payload = "<foo><version>1</version></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    ModelloUtils.load("3", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER_FAILING));
  }

  @Test
  public void upgradeNoConverter() throws Exception {
    final String payload = "<foo><version>1</version></foo>";
    final File file = util.createTempFile();
    FileSupport.writeFile(file.toPath(), payload);

    try {
      final Xpp3Dom dom = ModelloUtils.load("99", file, DOM_READER, Arrays.asList(V1_V2_UPGRADER, V2_V3_UPGRADER));
    }
    catch (IOException e) {
      assertThat(e.getMessage(), startsWith("Could not upgrade model to version 99"));
    }
  }

  @Test
  public void plainSave() throws Exception {
    final String payload = "<foo><version>1</version></foo>";
    final File file = util.createTempFile();
    Files.delete(file.toPath());

    final Xpp3Dom model = Xpp3DomBuilder.build(new StringReader(payload));

    ModelloUtils.save(model, file, DOM_WRITER);

    assertThat(file, exists());
    assertThat(FileSupport.readFile(file.toPath()), equalTo(
        "<foo>\n  <version>1</version>\n</foo>"));
  }

}
