/**
 *
 */
package de.is24.nexus.yum.alias.domain;

/**
 * @author BVoss
 *
 */
public class AliasKey {
  private final String repoId;

  private final String alias;

  public AliasKey(String repoId, String alias) {
    this.repoId = repoId;
    this.alias = alias;
  }

  public String getRepoId() {
    return repoId;
  }

  public String getAlias() {
    return alias;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((alias == null) ? 0 : alias.hashCode());
    result = (prime * result) + ((repoId == null) ? 0 : repoId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    AliasKey other = (AliasKey) obj;
    if (alias == null) {
      if (other.alias != null) {
        return false;
      }
    } else if (!alias.equals(other.alias)) {
      return false;
    }
    if (repoId == null) {
      if (other.repoId != null) {
        return false;
      }
    } else if (!repoId.equals(other.repoId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AliasKey [repoId=" + repoId + ", alias=" + alias + "]";
  }
}
