/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

import com.jcraft.jzlib.GZIPInputStream;

/**
 * to make a HTTP POST to hosted repository allows only two path:
 * <li>/api/v1/gem</li>
 * <li>/gems/{name-version(-platform)}.gem</li>
 *
 * all other paths are forbidden.
 *
 * when uploading a gem all the specs.4.8 files will be updated.
 *
 * the dependency file for the gemname and gemspec file will be generated on
 * demand.
 *
 * @author christian
 * @see HostedGETLayout
 */
public class HostedPOSTLayout
    extends NoopDefaultLayout
{
  public HostedPOSTLayout(RubygemsGateway gateway, Storage store) {
    super(gateway, store);
  }

  @Override
  public void addGem(InputStream in, RubygemsFile file) {
    if (file.type() != FileType.GEM && file.type() != FileType.API_V1) {
      throw new RuntimeException("BUG: not allowed to store " + file);
    }
    try {
      store.create(in, file);
      if (file.hasNoPayload()) {
        // an error or something else but we need the payload now
        return;
      }
      Object spec;
      try(InputStream is = store.getInputStream(file)) {
        spec = gateway.spec(is);
      }

      String filename = gateway.filename(spec);
      // check gemname matches coordinates from its specification
      switch (file.type()) {
        case GEM:
          if (!(((GemFile) file).filename() + ".gem").equals(filename)) {
            store.delete(file);
            // now set the error for further processing
            file.setException(new IOException("filename from " + file + " does not match gemname: " + filename));
            return;
          }
          break;
        case API_V1:
          store.create(store.getInputStream(file),
              ((ApiV1File) file).gem(filename));
          store.delete(file);
          break;
        default:
          throw new RuntimeException("BUG");
      }

      addSpecToIndex(spec);

      // delete dependencies so the next request will recreate it
      delete(super.dependencyFile(gateway.name(spec)));
      // delete gemspec so the next request will recreate it
      delete(super.gemspecFile(gateway.filename(spec).replaceFirst(".gem$", "")));
    }
    catch (IOException e) {
      file.setException(e);
    }
  }

  /**
   * add a spec (Ruby Object) to the specs.4.8 indices.
   */
  private void addSpecToIndex(Object spec) throws IOException {
    for (SpecsIndexType type : SpecsIndexType.values()) {
      InputStream content = null;
      SpecsIndexZippedFile specs = ensureSpecsIndexZippedFile(type);

      try (InputStream in = new GZIPInputStream(store.getInputStream(specs))) {
        content = gateway.addSpec(spec, in, type);

        // if nothing was added the content is NULL
        if (content != null) {
          store.update(IOUtil.toGzipped(content), specs);
          if (specs.hasException()) {
            throw new IOException(specs.getException());
          }
        }
      }
      finally {
        IOUtil.close(content);
      }
    }
  }

  @Override
  public ApiV1File apiV1File(String name) {
    ApiV1File apiV1 = super.apiV1File(name);
    if (!"api_key".equals(apiV1.name())) {
      apiV1.markAsForbidden();
    }
    return apiV1;
  }

  @Override
  public SpecsIndexFile specsIndexFile(SpecsIndexType type) {
    SpecsIndexFile file = super.specsIndexFile(type);
    file.markAsForbidden();
    return file;
  }

  @Override
  public SpecsIndexZippedFile specsIndexZippedFile(SpecsIndexType type) {
    SpecsIndexZippedFile file = super.specsIndexZippedFile(type);
    file.markAsForbidden();
    return file;
  }

  @Override
  public SpecsIndexFile specsIndexFile(String name) {
    SpecsIndexFile file = super.specsIndexFile(name);
    file.markAsForbidden();
    return file;
  }

  @Override
  public SpecsIndexZippedFile specsIndexZippedFile(String name) {
    SpecsIndexZippedFile file = super.specsIndexZippedFile(name);
    file.markAsForbidden();
    return file;
  }

  @Override
  public GemspecFile gemspecFile(String name, String version, String platform) {
    GemspecFile file = super.gemspecFile(name, version, platform);
    file.markAsForbidden();
    return file;
  }

  @Override
  public GemspecFile gemspecFile(String name) {
    GemspecFile file = super.gemspecFile(name);
    file.markAsForbidden();
    return file;
  }

  @Override
  public DependencyFile dependencyFile(String name) {
    DependencyFile file = super.dependencyFile(name);
    file.markAsForbidden();
    return file;
  }
}