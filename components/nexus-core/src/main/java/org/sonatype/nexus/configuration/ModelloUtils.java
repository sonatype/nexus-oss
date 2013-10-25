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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple utilities to handle Modello generated models CProps to Map converter, to ease handling of CProps. All
 * these methods are specific to Modello use in Nexus, but still, are generic enough to be used by Nexus Plugins
 * if needed.
 *
 * @author cstamas
 */
public class ModelloUtils
{
  private ModelloUtils() {
    // no instance
  }

  // ==

  /**
   * {@link List} of {@link CProps} to {@link Map} converter, to ease handling of these thingies.
   */
  public static Map<String, String> getMapFromConfigList(List<CProps> list) {
    final Map<String, String> result = Maps.newHashMapWithExpectedSize(list.size());
    for (CProps props : list) {
      result.put(props.getKey(), props.getValue());
    }
    return result;
  }

  /**
   * {@link Map} to {@link List} of {@link CProps} converter, to ease handling of these thingies.
   */
  public static List<CProps> getConfigListFromMap(final Map<String, String> map) {
    final List<CProps> result = Lists.newArrayListWithExpectedSize(map.size());
    for (Map.Entry<String, String> entry : map.entrySet()) {
      final CProps cprop = new CProps();
      cprop.setKey(entry.getKey());
      cprop.setValue(entry.getValue());
      result.add(cprop);
    }
    return result;
  }

  // == Model IO

  /**
   * Modello models are by default all UTF-8.
   *
   * @since 2.7.0
   */
  public static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

  /**
   * Model reader.
   *
   * @since 2.7.0
   */
  public static interface ModelReader<E>
  {
    E read(Reader reader)
        throws IOException, XmlPullParserException;
  }

  /**
   * Model writer.
   *
   * @since 2.7.0
   */
  public static interface ModelWriter<E>
  {
    void write(Writer writer, E model)
        throws IOException;
  }

  /**
   * Model upgrader.
   *
   * @since 2.7.0
   */
  public static interface ModelUpgrader
  {
    String fromVersion();

    String toVersion();

    void upgrade(Reader reader, Writer writer)
        throws IOException, XmlPullParserException;
  }

  /**
   * Adapter for {@link ModelUpgrader} to be used with {@link FileReplacer}.
   */
  private static class ModelUpgraderAdapter
      implements ContentWriter
  {
    private final File file;

    private final ModelUpgrader modelUpgrader;

    private ModelUpgraderAdapter(final File file, final ModelUpgrader modelUpgrader) {
      this.file = checkNotNull(file);
      this.modelUpgrader = checkNotNull(modelUpgrader);
    }

    @Override
    public void write(final BufferedOutputStream output) throws IOException {
      try (final Reader reader = Files.newBufferedReader(file.toPath(), DEFAULT_CHARSET)) {
        modelUpgrader.upgrade(reader,
            new OutputStreamWriter(output, DEFAULT_CHARSET));
      }
      catch (XmlPullParserException e) {
        throw new XmlPullParserExceptionRT(e);
      }
    }
  }

  /**
   * Trick class to make the XML parsing exception go through {@link FileReplacer}.
   */
  private static class XmlPullParserExceptionRT
      extends RuntimeException
  {
    private XmlPullParserExceptionRT(final XmlPullParserException cause) {
      super(cause);
    }

    @Override
    public XmlPullParserException getCause() {
      return (XmlPullParserException) super.getCause();
    }
  }

  /**
   * Loads a model from a file using given writer. Also, checks the file version and if does not match with given
   * {@code currentModelVersion} will attempt upgrade using passed in upgraders.
   * <p/>
   * Note: this method method assumes few things about model: it is suited for Modello generated XML models,
   * modelled in "nexus way". They are expected to have 1st level sibling, having name {@code "version"} carrying
   * the version of the given XML model. The versions are opaque, they are not sorted and such, they are checked
   * for plain equality. XML Models <em>without</em> "version" node, or having it's "version" node empty
   * will be rejected with XmlPullParserException, kinda considered corrupt.
   * <p/>
   * Also, be aware that this method, even while loading the XML file and converting it into POJOs, will not
   * perform any semantic validation of it, that's the caller's duty to perform. In case of IO problem, or
   * corrupted or XML file parsing failures, proper exception is thrown. So to say, only "syntactic" analysis
   * happens here (XML is well formed and readable).
   * <p/>
   * Concurrency note: if concurrent invocation of this (thread safe method) is possible at client side,
   * it's is caller role to ensure synchronization in caller code and make sure this method is not called
   * concurrently, as IO side effects will have unexpected results. Invoking it multiple times is fine,
   * but simultaneous invocation from same component (working on same file) should not happen.
   *
   * @since 2.7.0
   */
  public static <E> E load(final String currentModelVersion,
                           final File file,
                           final ModelReader<E> reader,
                           final List<ModelUpgrader> upgraders)
      throws XmlPullParserException, IOException
  {
    checkNotNull(currentModelVersion, "currentModelVersion");
    checkNotNull(file, "file");
    checkNotNull(reader, "reader");
    checkNotNull(upgraders, "upgraders");
    final String originalFileVersion;
    try (final Reader r = Files.newBufferedReader(file.toPath(), DEFAULT_CHARSET)) {
      final Xpp3Dom dom = Xpp3DomBuilder.build(r);
      final Xpp3Dom versionNode = dom.getChild("version");
      if (versionNode != null) {
        originalFileVersion = versionNode.getValue();
        if (Strings.isNullOrEmpty(originalFileVersion)) {
          throw new XmlPullParserException("Passed in XML model have empty version node!");
        }
      }
      else {
        throw new XmlPullParserException("Passed in XML model does not have version node!");
      }
    }

    if (!Objects.equals(currentModelVersion, originalFileVersion)) {
      // need upgrade
      String currentFileVersion = originalFileVersion;
      final Map<String, ModelUpgrader> upgradersMap = Maps.newHashMapWithExpectedSize(upgraders.size());
      for (ModelUpgrader upgrader : upgraders) {
        upgradersMap.put(upgrader.fromVersion(), upgrader);
      }
      final FileReplacer fileReplacer = new FileReplacer(file);
      fileReplacer.setDeleteBackupFile(true);
      ModelUpgrader upgrader = upgradersMap.get(currentFileVersion);
      while (upgrader != null && !Objects.equals(currentModelVersion, currentFileVersion)) {
        try {
          fileReplacer.replace(new ModelUpgraderAdapter(file, upgrader));
        }
        catch (XmlPullParserExceptionRT e) {
          // "peel off" the RT exception, as client code would handle XmlPullParserException anyway, is Modello specific
          final XmlPullParserException ex = new XmlPullParserException(String
              .format("Problem during upgrade step from %s to %s", upgrader.fromVersion(), upgrader.toVersion()));
          ex.initCause(e.getCause());
          throw ex;
        }
        catch (IOException e) {
          final IOException ex = new IOException(String
              .format("Problem during upgrade step from %s to %s", upgrader.fromVersion(), upgrader.toVersion()), e);
          throw ex;
        }
        currentFileVersion = upgrader.toVersion();
        upgrader = upgradersMap.get(currentFileVersion);
      }

      if (!Objects.equals(currentModelVersion, currentFileVersion)) {
        // upgrade failed
        throw new IOException(String
            .format(
                "Could not upgrade model to version %s, is upgraded to %s, originally was %s, available upgraders exists for versions %s",
                currentModelVersion, currentFileVersion, originalFileVersion, upgradersMap.keySet()));
      }
    }

    try (final Reader fr = Files.newBufferedReader(file.toPath(), DEFAULT_CHARSET)) {
      E model = reader.read(fr);
      // model.setVersion(currentModelVersion);
      return model;
    }
  }

  /**
   * Saves a model to a file using given writer, keeping the a backup of the file.
   * <p/>
   * Concurrency note: if concurrent invocation of this (thread safe method) is possible at client side,
   * it's is caller role to ensure synchronization in caller code and make sure this method is not called
   * concurrently, as IO side effects will have unexpected results. Invoking it multiple times is fine,
   * but simultaneous invocation from same component (working on same file) should not happen.
   *
   * @since 2.7.0
   */
  public static <E> void save(final E model, final File file, final ModelWriter<E> writer) throws IOException {
    checkNotNull(model, "model");
    checkNotNull(file, "File");
    checkNotNull(writer, "ModelWriter");
    Files.createDirectories(file.getParentFile().toPath());
    // we would need to have "last" backups but FileReplacer would need a change here
    final FileReplacer fileReplacer = new FileReplacer(file);
    fileReplacer.setDeleteBackupFile(true);
    fileReplacer.replace(new ContentWriter()
    {
      @Override
      public void write(final BufferedOutputStream output) throws IOException {
        final OutputStreamWriter w = new OutputStreamWriter(output, DEFAULT_CHARSET);
        writer.write(w, model);
        w.flush();
      }
    });
  }
}
