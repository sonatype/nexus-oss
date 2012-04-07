package de.is24.nexus.yum.repository;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpmListWriter {
  private static final int POSITION_AFTER_SLASH = 1;
  private static final Logger LOG = LoggerFactory.getLogger(RpmListWriter.class);

  private final File rpmListFile;
  private final String version;
  private final String addedFiles;
  private final String repositoryId;
  private final String baseRpmDir;
  private final boolean singleRpmPerDirectory;
  private final ListFileFactory fileFactory;

  public RpmListWriter(String repositoryId, String baseRpmDir, String addedFiles, String version, boolean singleRpmPerDirectory,
      ListFileFactory fileFactory) {
    this.repositoryId = repositoryId;
    this.baseRpmDir = baseRpmDir;
    this.addedFiles = addedFiles;
    this.version = version;
    this.singleRpmPerDirectory = singleRpmPerDirectory;
    this.fileFactory = fileFactory;
    this.rpmListFile = fileFactory.getRpmListFile(repositoryId);
  }

  public File writeList() throws IOException {
    if (rpmListFile.exists()) {
      LOG.info("Reuse existing rpm list file : {}", rpmListFile);
      List<String> rpmFileList = pruneToExistingRpms();

      if (isNotBlank(version)) {
        return extractVersionOfListFile(rpmFileList);
      }

      if (isNotBlank(addedFiles)) {
        addNewlyAddedRpmFileToList(rpmFileList);
      }

      writeRpmFileList(rpmFileList);
    } else {
      rewriteList();
    }

    return rpmListFile;
  }

  private File extractVersionOfListFile(List<String> files) throws IOException {
    List<String> filesWithRequiredVersion = new ArrayList<String>();
    for (String file : files) {
      if (hasRequiredVersion(file)) {
        filesWithRequiredVersion.add(file);
      }
    }

    File rpmVersionizedListFile = fileFactory.getRpmListFile(repositoryId, version);
    writeRpmFileList(filesWithRequiredVersion, rpmVersionizedListFile);
    return rpmVersionizedListFile;
  }

  private boolean hasRequiredVersion(String file) {
    String[] segments = file.split("\\/");
    return (segments.length >= 2) && version.equals(segments[segments.length - 2]);
  }

  private void addNewlyAddedRpmFileToList(List<String> fileList) throws IOException {
    final String[] filenames = addedFiles.split(pathSeparator);
    for (String filename : filenames) {
      filename = addFileToList(fileList, filename);
    }
  }

  private String addFileToList(List<String> fileList, String filename) {
    final int startPosition = filename.startsWith("/") ? POSITION_AFTER_SLASH : 0;
    filename = filename.substring(startPosition);

    if (!fileList.contains(filename)) {
      fileList.add(filename);
      LOG.info("Added rpm {} to file list.", filename);
    } else {
      LOG.info("Rpm {} already exists in file list.", filename);
    }
    return filename;
  }

  private List<String> pruneToExistingRpms() throws IOException {
    List<String> files = readRpmFileList();
    for (int i = 0; i < files.size(); i++) {
      if (!new File(baseRpmDir, files.get(i)).exists()) {
        LOG.info("Removed {} from rpm list.", files.get(i));
        files.remove(i);
        i--;

      }
    }
    return files;
  }

  private void writeRpmFileList(Collection<String> files) throws IOException {
    writeRpmFileList(files, rpmListFile);
  }

  private void writeRpmFileList(Collection<String> files, File rpmListOutputFile) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(rpmListOutputFile);
    try {
      writeLines(files, "\n", outputStream);
      if (files.isEmpty()) {
        LOG.info(
            "Write non existing package to rpm list file {} to avoid an empty packge list that would cause createrepo to scan the whole directory",
            rpmListOutputFile);
        write(".foo/.bar.rpm/to-avoid-an-empty-rpm-list-file/that-would-cause-createrepo-to-scan-the-whole-repo.rpm", outputStream);
      }
    } finally {
      outputStream.close();
    }
    LOG.info("Wrote {} rpm packages to rpm list file {} .", files.size(), rpmListFile);
  }

  @SuppressWarnings("unchecked")
  private List<String> readRpmFileList() throws IOException {
    FileInputStream inputStream = new FileInputStream(rpmListFile);
    try {
      return readLines(inputStream);
    } finally {
      inputStream.close();
    }
  }

  private void rewriteList() throws IOException {
    if (singleRpmPerDirectory) {
      rewriteFileList(getSortedFilteredFileList());
    } else {
      writeRpmFileList(getRelativeFilenames(getRpmFileList()), rpmListFile);
    }
  }

  private List<String> getRelativeFilenames(Collection<File> rpmFileList) {
    String absoluteBasePath = baseRpmDir + separator;

    List<String> result = new ArrayList<String>(rpmFileList.size());
    for (File rpmFile : rpmFileList) {
      result.add(getRelativePath(rpmFile, absoluteBasePath));
    }
    return result;
  }

  private void rewriteFileList(Map<String, String> fileMap) {
    try {
      Writer writer = new FileWriter(rpmListFile);
      try {
        for (Entry<String, String> entry : fileMap.entrySet()) {
          writer.append(format("%s%s\n", entry.getKey(), entry.getValue()));
        }
      } finally {
        writer.close();
      }
      LOG.info("Wrote temporary package list to {}", rpmListFile.getAbsoluteFile());
    } catch (IOException e) {
      LOG.warn("Could not write temporary package list file", e);
    }

  }

  private Map<String, String> getSortedFilteredFileList() {
    String absoluteBasePath = baseRpmDir + separator;

    Map<String, String> fileMap = new TreeMap<String, String>();

    for (File file : getRpmFileList()) {
      File parentFile = file.getParentFile();
      if (matchesRequestedVersion(parentFile)) {
        String parentDir = getRelativePath(parentFile, absoluteBasePath);
        putLatestArtifactInMap(parentDir, file.getName(), fileMap);
      }
    }
    return fileMap;
  }

  private void putLatestArtifactInMap(String parentDir, String filename, Map<String, String> fileMap) {
    if (!fileMap.containsKey(parentDir) || (filename.compareTo(fileMap.get(parentDir)) > 0)) {
      fileMap.put(parentDir, filename);
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<File> getRpmFileList() {
    Collection<File> result = listFiles(new File(baseRpmDir), new String[] { "rpm" }, true);
    return result;
  }

  private String getRelativePath(File file, String baseDirectory) {
    String filePath = file.getAbsolutePath() + (file.isDirectory() ? separator : "");
    if (filePath.startsWith(baseDirectory)) {
      filePath = filePath.substring(baseDirectory.length());
    }
    return filePath;
  }

  private boolean matchesRequestedVersion(File parentFile) {
    return (version == null) || parentFile.getName().equals(version);
  }

}
