package org.sonatype.nexus.blobstore.file;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.Type;

@Named(FileType.NAME)
@Singleton
public class FileType extends Type
{
  public static final String NAME = "file";
  
  public FileType() {
    super(NAME);
  }
}
