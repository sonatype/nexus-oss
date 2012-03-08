package de.is24.nexus.yum.repository.config;

import java.io.File;

import de.is24.nexus.yum.repository.task.YumMetadataGenerationTask;


/**
 * Configuration holding all information needed to generate a Yum repository.
 * It's typically created via the {@link YumGeneratorConfigurationBuilder} and
 * handed over to the {@link YumMetadataGenerationTask}.
 * 
 * @author sherold
 * 
 */
public interface YumGeneratorConfiguration {
  /**
   * @return the base directory where all RPMs are in.
   */
  File getBaseRpmDir();

  /**
   * @return the url where the {@link #getBaseRpmDir() base rpm dir} is
   *         accessable from outside (via HTTP).
   */
  String getBaseRpmUrl();

  /**
   * @return the directory where the resulting yum repository should be saved.
   */
  File getBaseRepoDir();

  /**
   * @return the url where the {@link #getBaseRepoDir() base repository dir} is
   *         accessable form outside (via HTTP)
   */
  String getBaseRepoUrl();

  /**
   * @return the id of the repository
   */
  String getId();

  /**
   * @return the version, if it's a versionized view on the RPMs or
   *         <code>null</code>, if the repository should contain all versions
   */
  String getVersion();

  /**
   * @return the base directory for cache files
   */
  File getBaseCacheDir();

  /**
   * The property is used, when you just want a add a single file to a yum
   * repository. The path have to begin with a slash and is relative to the
   * {@link #getBaseRpmDir() rpm base directory}.
   *
   * @return relative path to a newly added file or <code>null</code>, if the
   *         whole yum-repository should be regenerated.
   */
  String getAddedFile();

  /**
   * The property controls whether you want to have just on RPM per directory.
   * This is a nice optimization for Nexus typical directory structure, where
   * mutiple snapshot-RPMs are in the same directory. For the repository-RPMs
   * this feature will be switched off.
   *
   * @return <code>true</code> or <code>false</code>
   */
  boolean isSingleRpmPerDirectory();

	/**
	 * @param config
	 *          another config to ckeck against
	 * @return <code>true</code>, if both configs should not be executed at ones
	 */
	boolean conflictsWith(YumGeneratorConfiguration config);

}
