package org.sonatype.nexus.coreui

import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty

/**
 * Repository status exchange object.
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class RepositoryStatusXO
{
  /**
   * Name of associated Repository.
   */
  @NotEmpty
  String repositoryName

  /**
   * Whether or not the repository is online.
   */
  boolean online

  /**
   * A description of the status.
   */
  String description

  /**
   * A reason for the status.
   */
  String reason
}
