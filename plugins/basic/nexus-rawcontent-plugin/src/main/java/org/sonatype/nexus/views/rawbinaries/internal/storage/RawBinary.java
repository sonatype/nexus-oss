package org.sonatype.nexus.views.rawbinaries.internal.storage;

import java.io.InputStream;
import java.util.Date;

import org.joda.time.DateTime;

/**
 * @since 3.0
 */
public interface RawBinary
{
  String getPath();

  InputStream getInputStream();

  String getMimeType();

  DateTime getModifiedDate();
}
