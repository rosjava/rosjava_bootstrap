package org.ros.gradle_plugins;

import org.gradle.api.*;
import org.gradle.api.publish.maven.MavenPublication;

/**
 * Configures a Java project for use with ROS.
 *
 * - project.ros.mavenPath : location of local ros maven repositories (in your chained workspaces)
 * - project.ros.mavenDeploymentRepository : location of the ros maven repository you will publish to
 *
 * It also performs the following actions
 *
 * - checks and makes sure the maven plugin is running
 * - constructs the sequence of dependent maven repos (local ros maven repos, mavenLocal, external ros maven repo)
 * - configures the uploadArchives for artifact deployment to the local ros maven repo (devel/share/maven)
 */
class RosPlugin implements Plugin<Project> {

  def void apply(Project project) {
    project.apply plugin: "maven"

    project.extensions.create("ros", RosPluginExtension)

    project.ros.mavenRepository = System.getenv("ROS_MAVEN_REPOSITORY")
    project.ros.mavenDeploymentRepository = System.getenv("ROS_MAVEN_DEPLOYMENT_REPOSITORY")
    String mavenPath = System.getenv("ROS_MAVEN_PATH")
    if (mavenPath != null) {
      project.ros.mavenPath = mavenPath.tokenize(":")
    }
    project.repositories {
      if (project.ros.mavenPath != null) {
        project.ros.mavenPath.each { path ->
          maven {
            url project.uri(path)
          }
        }
      }
      if (project.ros.mavenRepository != null) {
        maven {
          url project.ros.mavenRepository
        }
      }
      /* 
       * This will often be the same as ROS_MAVEN_REPOSITORY, but this way it lets a user
       * provide a repository of their own via the environment variable and use this as a fallback.
       */
      maven {
        url "https://github.com/rosjava/rosjava_mvn_repo/raw/master"
      }
      mavenLocal()
      maven {
        url "http://repository.springsource.com/maven/bundles/release"
      }
      maven {
        url "http://repository.springsource.com/maven/bundles/external"
      }
      jcenter()
    }
  }
}

/* http://www.gradle.org/docs/nightly/dsl/org.gradle.api.plugins.ExtensionAware.html */
class RosPluginExtension {
  String mavenRepository
  String mavenDeploymentRepository
  List<String> mavenPath

  RosPluginExtension() {
    /* Initialising the strings here gets rid of the dynamic property deprecated warnings. */
    this.mavenDeploymentRepository = ""
    this.mavenRepository = ""
  }
}
