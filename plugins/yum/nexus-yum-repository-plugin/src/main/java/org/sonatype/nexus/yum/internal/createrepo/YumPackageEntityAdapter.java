/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.createrepo;

import java.util.List;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.nexus.yum.internal.createrepo.YumPackage.Entry;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link YumPackage} entity adapter.
 *
 * @since 3.0
 */
public class YumPackageEntityAdapter
    extends ComponentSupport
{

  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("yum")
      .type(YumPackage.class)
      .build();

  private static final String DB_CLASS_ENTRY = new OClassNameBuilder()
      .prefix("yum")
      .type(Entry.class)
      .build();

  private static final String P_REPOSITORY_ID = "repositoryId";

  private static final String P_PKG_ID = "pkgId";

  private static final String P_LOCATION = "location";

  private static final String P_CHECKSUM = "checksum";

  private static final String P_CHECKSUM_TYPE = "checksumType";

  private static final String P_NAME = "name";

  private static final String P_ARCH = "arch";

  private static final String P_EPOCH = "epoch";

  private static final String P_VERSION = "version";

  private static final String P_RELEASE = "release";

  private static final String P_SUMMARY = "summary";

  private static final String P_DESCRIPTION = "description";

  private static final String P_URL = "url";

  private static final String P_TIME_FILE = "timeFile";

  private static final String P_TIME_BUILD = "timeBuild";

  private static final String P_RPM_LICENSE = "rpmLicense";

  private static final String P_RPM_VENDOR = "rpmVendor";

  private static final String P_RPM_GROUP = "rpmGroup";

  private static final String P_RPM_BUILDHOST = "rpmBuildHost";

  private static final String P_RPM_SOURCERPM = "rpmSourceRpm";

  private static final String P_RPM_HEADER_START = "rpmHeaderStart";

  private static final String P_RPM_HEADER_END = "rpmHeaderEnd";

  private static final String P_PACKAGER = "packager";

  private static final String P_SIZE_PACKAGE = "sizePackage";

  private static final String P_SIZE_INSTALLED = "sizeInstalled";

  private static final String P_SIZE_ARCHIVE = "sizeArchive";

  private static final String P_PROVIDES = "provides";

  private static final String P_REQUIRES = "requires";

  private static final String P_CONFLICTS = "conflicts";

  private static final String P_OBSOLETES = "obsoletes";

  private static final String P_FILES = "files";

  private static final String P_CHANGES = "changes";

  private static final String I_REPOSITORY_ID_LOCATION = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_REPOSITORY_ID)
      .property(P_LOCATION)
      .build();

  private final YumPackageEntryEntityAdapter entryEntityAdapter = new YumPackageEntryEntityAdapter();

  private final YumPackageFileEntityAdapter fileEntityAdapter = new YumPackageFileEntityAdapter();

  private final YumPackageChangeLogEntityAdapter changeLogEntityAdapter = new YumPackageChangeLogEntityAdapter();

  /**
   * Register schema.
   */
  OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      OClass entryType = entryEntityAdapter.register(db);
      OClass fileType = fileEntityAdapter.register(db);
      OClass changeLogType = changeLogEntityAdapter.register(db);

      type = schema.createClass(DB_CLASS);
      type.createProperty(P_REPOSITORY_ID, OType.STRING).setNotNull(true).setMandatory(true);
      type.createProperty(P_LOCATION, OType.STRING).setNotNull(true).setMandatory(true);
      type.createProperty(P_PKG_ID, OType.STRING).setNotNull(true).setMandatory(true);
      type.createProperty(P_CHECKSUM, OType.STRING);
      type.createProperty(P_CHECKSUM_TYPE, OType.STRING);
      type.createProperty(P_NAME, OType.STRING);
      type.createProperty(P_ARCH, OType.STRING);
      type.createProperty(P_VERSION, OType.STRING);
      type.createProperty(P_EPOCH, OType.STRING);
      type.createProperty(P_RELEASE, OType.STRING);
      type.createProperty(P_SUMMARY, OType.STRING);
      type.createProperty(P_DESCRIPTION, OType.STRING);
      type.createProperty(P_URL, OType.STRING);
      type.createProperty(P_TIME_FILE, OType.INTEGER);
      type.createProperty(P_TIME_BUILD, OType.INTEGER);
      type.createProperty(P_RPM_LICENSE, OType.STRING);
      type.createProperty(P_RPM_VENDOR, OType.STRING);
      type.createProperty(P_RPM_GROUP, OType.STRING);
      type.createProperty(P_RPM_BUILDHOST, OType.STRING);
      type.createProperty(P_RPM_SOURCERPM, OType.STRING);
      type.createProperty(P_RPM_HEADER_START, OType.INTEGER);
      type.createProperty(P_RPM_HEADER_END, OType.INTEGER);
      type.createProperty(P_PACKAGER, OType.STRING);
      type.createProperty(P_SIZE_PACKAGE, OType.INTEGER);
      type.createProperty(P_SIZE_INSTALLED, OType.INTEGER);
      type.createProperty(P_SIZE_ARCHIVE, OType.INTEGER);
      type.createProperty(P_PROVIDES, OType.EMBEDDEDLIST, entryType);
      type.createProperty(P_REQUIRES, OType.EMBEDDEDLIST, entryType);
      type.createProperty(P_CONFLICTS, OType.EMBEDDEDLIST, entryType);
      type.createProperty(P_OBSOLETES, OType.EMBEDDEDLIST, entryType);
      type.createProperty(P_FILES, OType.EMBEDDEDLIST, fileType);
      type.createProperty(P_CHANGES, OType.EMBEDDEDLIST, changeLogType);

      type.createIndex(I_REPOSITORY_ID_LOCATION, INDEX_TYPE.UNIQUE, P_REPOSITORY_ID, P_LOCATION);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  ODocument create(final ODatabaseDocumentTx db, final String repositoryId, final YumPackage entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(db, doc, repositoryId, entity);
  }

  /**
   * Write entity to document.
   */
  ODocument write(final ODatabaseDocumentTx db, final ODocument document, final String repositoryId,
                  final YumPackage entity)
  {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_REPOSITORY_ID, repositoryId);
    document.field(P_LOCATION, entity.getLocation());
    document.field(P_PKG_ID, entity.getPkgId());
    document.field(P_CHECKSUM, entity.getChecksum());
    document.field(P_CHECKSUM_TYPE, entity.getChecksumType());
    document.field(P_NAME, entity.getName());
    document.field(P_ARCH, entity.getArch());
    document.field(P_VERSION, entity.getVersion());
    document.field(P_EPOCH, entity.getEpoch());
    document.field(P_RELEASE, entity.getRelease());
    document.field(P_SUMMARY, entity.getSummary());
    document.field(P_DESCRIPTION, entity.getDescription());
    document.field(P_URL, entity.getUrl());
    document.field(P_TIME_FILE, entity.getTimeFile());
    document.field(P_TIME_BUILD, entity.getTimeBuild());
    document.field(P_RPM_LICENSE, entity.getRpmLicense());
    document.field(P_RPM_VENDOR, entity.getRpmVendor());
    document.field(P_RPM_GROUP, entity.getRpmGroup());
    document.field(P_RPM_BUILDHOST, entity.getRpmBuildHost());
    document.field(P_RPM_SOURCERPM, entity.getRpmSourceRpm());
    document.field(P_RPM_HEADER_START, entity.getRpmHeaderStart());
    document.field(P_RPM_HEADER_END, entity.getRpmHeaderEnd());
    document.field(P_PACKAGER, entity.getPackager());
    document.field(P_SIZE_PACKAGE, entity.getSizePackage());
    document.field(P_SIZE_INSTALLED, entity.getSizeInstalled());
    document.field(P_SIZE_ARCHIVE, entity.getSizeArchive());
    document.field(P_PROVIDES, entryEntityAdapter.create(db, entity.getProvides()));
    document.field(P_REQUIRES, entryEntityAdapter.create(db, entity.getRequires()));
    document.field(P_CONFLICTS, entryEntityAdapter.create(db, entity.getConflicts()));
    document.field(P_OBSOLETES, entryEntityAdapter.create(db, entity.getObsoletes()));
    document.field(P_FILES, fileEntityAdapter.create(db, entity.getFiles()));
    document.field(P_CHANGES, changeLogEntityAdapter.create(db, entity.getChanges()));

    return document.save();
  }

  /**
   * Read entity from document.
   */
  YumPackage read(final ODocument document) {
    checkNotNull(document);

    YumPackage entity = new YumPackage();
    entity.setLocation(document.<String>field(P_LOCATION, OType.STRING));
    entity.setPkgId(document.<String>field(P_PKG_ID, OType.STRING));
    entity.setChecksum(document.<String>field(P_CHECKSUM, OType.STRING));
    entity.setChecksumType(document.<String>field(P_CHECKSUM_TYPE, OType.STRING));
    entity.setName(document.<String>field(P_NAME, OType.STRING));
    entity.setArch(document.<String>field(P_ARCH, OType.STRING));
    entity.setVersion(document.<String>field(P_VERSION, OType.STRING));
    entity.setEpoch(document.<String>field(P_EPOCH, OType.STRING));
    entity.setRelease(document.<String>field(P_RELEASE, OType.STRING));
    entity.setSummary(document.<String>field(P_SUMMARY, OType.STRING));
    entity.setDescription(document.<String>field(P_DESCRIPTION, OType.STRING));
    entity.setUrl(document.<String>field(P_URL, OType.STRING));
    entity.setTimeFile(document.<Integer>field(P_TIME_FILE, OType.INTEGER));
    entity.setTimeBuild(document.<Integer>field(P_TIME_BUILD, OType.INTEGER));
    entity.setRpmLicense(document.<String>field(P_RPM_LICENSE, OType.STRING));
    entity.setRpmVendor(document.<String>field(P_RPM_VENDOR, OType.STRING));
    entity.setRpmGroup(document.<String>field(P_RPM_GROUP, OType.STRING));
    entity.setRpmBuildHost(document.<String>field(P_RPM_BUILDHOST, OType.STRING));
    entity.setRpmSourceRpm(document.<String>field(P_RPM_SOURCERPM, OType.STRING));
    entity.setRpmHeaderStart(document.<Integer>field(P_RPM_HEADER_START, OType.INTEGER));
    entity.setRpmHeaderEnd(document.<Integer>field(P_RPM_HEADER_END, OType.INTEGER));
    entity.setPackager(document.<String>field(P_PACKAGER, OType.STRING));
    entity.setSizePackage(document.<Integer>field(P_SIZE_PACKAGE, OType.INTEGER));
    entity.setSizeInstalled(document.<Integer>field(P_SIZE_INSTALLED, OType.INTEGER));
    entity.setSizeArchive(document.<Integer>field(P_SIZE_ARCHIVE, OType.INTEGER));
    entity.setProvides(entryEntityAdapter.read(document.<List<ODocument>>field(P_PROVIDES, OType.EMBEDDEDLIST)));
    entity.setRequires(entryEntityAdapter.read(document.<List<ODocument>>field(P_REQUIRES, OType.EMBEDDEDLIST)));
    entity.setConflicts(entryEntityAdapter.read(document.<List<ODocument>>field(P_CONFLICTS, OType.EMBEDDEDLIST)));
    entity.setObsoletes(entryEntityAdapter.read(document.<List<ODocument>>field(P_OBSOLETES, OType.EMBEDDEDLIST)));
    entity.setFiles(fileEntityAdapter.read(document.<List<ODocument>>field(P_FILES, OType.EMBEDDEDLIST)));
    entity.setChanges(changeLogEntityAdapter.read(document.<List<ODocument>>field(P_CHANGES, OType.EMBEDDEDLIST)));

    return entity;
  }

  /**
   * Get all documents for repository.
   */
  Iterable<ODocument> get(final ODatabaseDocumentTx db, final String repositoryId) {
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
        "SELECT FROM " + DB_CLASS + " WHERE " + P_REPOSITORY_ID + " = ?"
    );
    return db.command(query).execute(repositoryId);
  }

  /**
   * Get document for repository/location.
   */
  ODocument get(final ODatabaseDocumentTx db, final String repositoryId, final String location) {
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
        "SELECT FROM " + DB_CLASS + " WHERE " + P_REPOSITORY_ID + " = ? AND " + P_LOCATION + " = ?"
    );
    List<ODocument> results = db.command(query).execute(repositoryId, location);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }

  /**
   * Delete all documents for repository.
   */
  void delete(final ODatabaseDocumentTx db, final String repositoryId) {
    OCommandSQL command = new OCommandSQL(
        "DELETE FROM " + DB_CLASS + " WHERE " + P_REPOSITORY_ID + " = ?"
    );
    db.command(command).execute(repositoryId);
  }

  /**
   * Delete documents for repository/location.
   */
  void delete(final ODatabaseDocumentTx db, final String repositoryId, final String location) {
    OCommandSQL command = new OCommandSQL(
        "DELETE FROM " + DB_CLASS + " WHERE " + P_REPOSITORY_ID + " = ? AND " + P_LOCATION + " LIKE ?"
    );
    db.command(command).execute(repositoryId, location + "%");
  }

}
