/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * @since 3.0
 */
public interface NugetProperties
{
  //-------------------------------------------------------------
  // ODATA metadata attributes, referenced in ODATA queries

  String AUTHORS = "AUTHORS";

  String COPYRIGHT = "COPYRIGHT";

  String CREATED = "CREATED";

  String DEPENDENCIES = "DEPENDENCIES";

  String DESCRIPTION = "DESCRIPTION";

  String DOWNLOAD_COUNT = "DOWNLOADCOUNT";

  String GALLERY_DETAILS_URL = "GALLERYDETAILSURL";

  String ICON_URL = "ICONURL";

  String ID = "ID";

  String IS_ABSOLUTE_LATEST_VERSION = "ISABSOLUTELATESTVERSION";

  String IS_LATEST_VERSION = "ISLATESTVERSION";

  String IS_PRERELEASE = "ISPRERELEASE";

  String LANGUAGE = "LANGUAGE";

  String LAST_UPDATED = "LASTUPDATED";

  String LICENSE_URL = "LICENSEURL";

  String LOCATION = "LOCATION";

  // Synthetic field to support Visual Studio's order by 'name'
  String NAME_ORDER = "NAME_ORDER";

  String PACKAGE_HASH = "PACKAGEHASH";

  String PACKAGE_HASH_ALGORITHM = "PACKAGEHASHALGORITHM";

  String PACKAGE_SIZE = "PACKAGESIZE";

  String PROJECT_URL = "PROJECTURL";

  String PUBLISHED = "PUBLISHED";

  String RELEASE_NOTES = "RELEASENOTES";

  String REPORT_ABUSE_URL = "REPORTABUSEURL";

  String REQUIRE_LICENSE_ACCEPTANCE = "REQUIRELICENSEACCEPTANCE";

  String SUMMARY = "SUMMARY";

  String TAGS = "TAGS";

  String TITLE = "TITLE";

  String VERSION = "VERSION";

  String VERSION_DOWNLOAD_COUNT = "VERSIONDOWNLOADCOUNT";

  //-------------------------------------------------------------
  // Component metadata property names, stored under attributes.nuget

  String P_AUTHORS = "authors";

  String P_COPYRIGHT = "copyright";

  String P_CREATED = "created";

  String P_DEPENDENCIES = "dependencies";

  String P_DESCRIPTION = "description";

  String P_DOWNLOAD_COUNT = "download_count";

  String P_GALLERY_DETAILS_URL = "gallery_details_url";

  String P_ICON_URL = "icon_url";

  String P_ID = "id";

  String P_IS_ABSOLUTE_LATEST_VERSION = "is_absolute_latest_version";

  String P_IS_LATEST_VERSION = "is_latest_version";

  String P_IS_PRERELEASE = "is_prerelease";

  String P_KEYWORDS = "keywords"; // to support search

  String P_LANGUAGE = "language";

  String P_LAST_UPDATED = "last_updated";

  String P_LICENSE_URL = "license_url";

  String P_LOCATION = "location";

  String P_PACKAGE_HASH = "package_hash";

  String P_PACKAGE_HASH_ALGORITHM = "package_hash_algorithm";

  String P_PACKAGE_SIZE = "package_size";

  String P_PROJECT_URL = "project_url";

  String P_PUBLISHED = "published";

  String P_RELEASE_NOTES = "release_notes";

  String P_REPORT_ABUSE_URL = "report_abuse_url";

  String P_REQUIRE_LICENSE_ACCEPTANCE = "require_license_acceptance";

  String P_SUMMARY = "summary";

  String P_TAGS = "tags";

  String P_TITLE = "title";

  // Derived field to support Visual Studio's order by 'name'
  String P_NAME_ORDER = "name_order";

  String P_VERSION = "version";

  String P_VERSION_DOWNLOAD_COUNT = "version_download_count";

  /*
  * A map from ODATA element name to the orientDB component attribute name under 'attributes.nuget'.
   */
  Map<String, String> ATTRIB_NAMES = new ImmutableMap.Builder<String, String>()
      .put(AUTHORS, P_AUTHORS)
      .put(COPYRIGHT, P_COPYRIGHT)
      .put(CREATED, P_CREATED)
      .put(DESCRIPTION, P_DESCRIPTION)
      .put(DOWNLOAD_COUNT, P_DOWNLOAD_COUNT)
      .put(ID, P_ID)
      .put(IS_ABSOLUTE_LATEST_VERSION, P_IS_ABSOLUTE_LATEST_VERSION)
      .put(IS_LATEST_VERSION, P_IS_LATEST_VERSION)
      .put(IS_PRERELEASE, P_IS_PRERELEASE)
      .put(LAST_UPDATED, P_LAST_UPDATED)
      .put(NAME_ORDER, P_NAME_ORDER)
      .put(PACKAGE_HASH, P_PACKAGE_HASH)
      .put(PACKAGE_HASH_ALGORITHM, P_PACKAGE_HASH_ALGORITHM)
      .put(PACKAGE_SIZE, P_PACKAGE_SIZE)
      .put(PUBLISHED, P_PUBLISHED)
      .put(REQUIRE_LICENSE_ACCEPTANCE, P_REQUIRE_LICENSE_ACCEPTANCE)
      .put(SUMMARY, P_SUMMARY)
      .put(TITLE, P_TITLE)
      .put(VERSION, P_VERSION)
      .put(VERSION_DOWNLOAD_COUNT, P_VERSION_DOWNLOAD_COUNT)
      .build();
}
