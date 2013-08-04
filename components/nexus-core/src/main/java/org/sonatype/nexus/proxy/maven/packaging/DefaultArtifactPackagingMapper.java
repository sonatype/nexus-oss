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

package org.sonatype.nexus.proxy.maven.packaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonatype.nexus.logging.AbstractLoggingComponent;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;

/**
 * A very simple artifact packaging mapper, that has everything for quick-start wired in this class. Also, it takes
 * into
 * account the "${nexus-work}/conf/packaging2extension-mapping.properties" file into account if found. To override the
 * "defaults" in this class, simply add lines to properties file with same keys.
 *
 * @author cstamas
 */
@Component(role = ArtifactPackagingMapper.class)
public class DefaultArtifactPackagingMapper
    extends AbstractLoggingComponent
    implements ArtifactPackagingMapper
{
  public static final String MAPPING_PROPERTIES_FILE = "packaging2extension-mapping.properties";

  private File propertiesFile;

  private volatile Map<String, String> packaging2extensionMapping;

  private final static Map<String, String> defaults;

  static {
    defaults = new HashMap<String, String>();
    defaults.put("ejb-client", "jar");
    defaults.put("ejb", "jar");
    defaults.put("rar", "jar");
    defaults.put("par", "jar");
    defaults.put("maven-plugin", "jar");
    defaults.put("maven-archetype", "jar");
    defaults.put("plexus-application", "jar");
    defaults.put("eclipse-plugin", "jar");
    defaults.put("eclipse-feature", "jar");
    defaults.put("eclipse-application", "zip");
    defaults.put("nexus-plugin", "jar");
    defaults.put("java-source", "jar");
    defaults.put("javadoc", "jar");
    defaults.put("test-jar", "jar");
    defaults.put("bundle", "jar");
  }

  public void setPropertiesFile(File propertiesFile) {
    this.propertiesFile = propertiesFile;
    this.packaging2extensionMapping = null;
  }

  public Map<String, String> getPackaging2extensionMapping() {
    if (packaging2extensionMapping == null) {
      synchronized (this) {
        if (packaging2extensionMapping == null) {
          packaging2extensionMapping = new HashMap<String, String>();

          // merge defaults
          packaging2extensionMapping.putAll(defaults);

          if (propertiesFile != null && propertiesFile.exists()) {
            getLogger().info("Found user artifact packaging mapping file, applying it...");

            Properties userMappings = new Properties();

            FileInputStream fis = null;

            try {
              fis = new FileInputStream(propertiesFile);

              userMappings.load(fis);

              if (userMappings.keySet().size() > 0) {
                for (Object key : userMappings.keySet()) {
                  packaging2extensionMapping.put(key.toString(),
                      userMappings.getProperty(key.toString()));
                }

                getLogger().info(
                    propertiesFile.getAbsolutePath()
                        + " user artifact packaging mapping file contained "
                        + userMappings.keySet().size() + " mappings, applied them all successfully.");
              }
            }
            catch (IOException e) {
              getLogger().warn(
                  "Got IO exception during read of file: " + propertiesFile.getAbsolutePath());
            }
            finally {
              IOUtil.close(fis);
            }

          }
          else {
            // make it silent if using defaults
            getLogger().debug(
                "User artifact packaging mappings file not found, will work with defaults...");
          }
        }
      }
    }

    return packaging2extensionMapping;
  }

  public void setPackaging2extensionMapping(Map<String, String> packaging2extensionMapping) {
    this.packaging2extensionMapping = packaging2extensionMapping;
  }

  public Map<String, String> getDefaults() {
    return defaults;
  }

  public String getExtensionForPackaging(String packaging) {
    if (packaging == null) {
      return "jar";
    }

    if (getPackaging2extensionMapping().containsKey(packaging)) {
      return getPackaging2extensionMapping().get(packaging);
    }
    else {
      // default's to packaging name, ie. "jar", "war", "pom", etc.
      return packaging;
    }
  }
}
