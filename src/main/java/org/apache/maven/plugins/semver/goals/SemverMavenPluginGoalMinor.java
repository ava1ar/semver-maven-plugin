package org.apache.maven.plugins.semver.goals;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.eclipse.jgit.api.errors.GitAPIException;

import org.apache.maven.plugins.semver.exceptions.SemverException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * <p>Determine MINOR version for MAVEN-project.</p>
 * <p>Example: move version x.1.x to x.2.x.</p>
 *
 * @author sido
 *
 */
@Mojo( name = "minor")
public class SemverMavenPluginGoalMinor extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();
    
    LOG.info("Semver-goal                       : MINOR");
    LOG.info("Run-mode                          : " + getConfiguration().getRunMode());
    LOG.info("Version from POM                  : " + version);
    LOG.info("SCM-connection                    : " + scmConnection);
    LOG.info("SCM-root                          : " + scmRoot);
    LOG.info(MOJO_LINE_BREAK);

    Map<RAW_VERSION, String> rawVersions = new HashMap<RAW_VERSION, String>();
    try {
      rawVersions = determineRawVersions(version);
    } catch (Exception e) {
      LOG.error(e);
    }

    executeRunMode(rawVersions);
    
  }


  /**
   *
   * <p>Determine MINOR version from POM-version.</p>
   *
   * @param version example: x.0.x-SNAPSHOT
   * @return list of development, git and release-version
   * @throws SemverException
   * @throws IOException
   * @throws GitAPIException
   */
  private Map<RAW_VERSION, String> determineRawVersions(String version) throws SemverException, IOException, GitAPIException {

    Map<RAW_VERSION, String> versions = new HashMap<RAW_VERSION, String>();

    int majorVersion = 0;
    int minorVersion = 1;
    int patchVersion = 0;

    String[] rawVersion = version.split("\\.");
    if (rawVersion.length > 0 && rawVersion.length == 3) {
      LOG.debug("Set version-variables from POM.xml");
      LOG.debug(MOJO_LINE_BREAK);
      majorVersion = Integer.valueOf(rawVersion[0]);
      minorVersion = Integer.valueOf(rawVersion[1]);
      patchVersion = Integer.valueOf(rawVersion[2].substring(0, rawVersion[2].lastIndexOf('-')));
    } else {
      LOG.error("Unrecognized version-pattern");
      LOG.error("Semver plugin is terminating");
      throw new SemverException("Unrecognized version-pattern", "Could not parse version from POM.xml because of not parseble version-pattern");
    }

    LOG.debug("MAJOR-version                    : " + majorVersion);
    LOG.debug("MINOR-version                    : " + minorVersion);
    LOG.debug("PATCH-version                    : " + patchVersion);
    LOG.debug(MOJO_LINE_BREAK);

    minorVersion = minorVersion + 1;
    patchVersion = 0;

    String developmentVersion = majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";
    String releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    String scmVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    if(getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH || getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH_HOSEE) {
      LOG.info("Determine new versions for branch : " + getConfiguration().getBranchVersion());
    }
    LOG.info("New DEVELOPMENT-version           : " + developmentVersion);
    LOG.info("New GIT-version                   : " + scmVersion);
    LOG.info("New RELEASE-version               : " + releaseVersion);
    LOG.info(MOJO_LINE_BREAK);

    versions.put(RAW_VERSION.DEVELOPMENT, developmentVersion);
    versions.put(RAW_VERSION.RELEASE, releaseVersion);
    versions.put(RAW_VERSION.MAJOR, String.valueOf(majorVersion));
    versions.put(RAW_VERSION.MINOR, String.valueOf(minorVersion));
    versions.put(RAW_VERSION.PATCH, String.valueOf(patchVersion));

    cleanupGitLocalAndRemoteTags(scmVersion);
    
    return versions;
  }

}
