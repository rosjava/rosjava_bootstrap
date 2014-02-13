package org.ros.gradle_plugins;

import org.gradle.api.*;
import org.gradle.api.publish.maven.MavenPublication;

/*
 * Provides information about the ros workspace.
 *
 * - project.ros.mavenPath : location of local ros maven repositories (in your chained workspaces)
 * - project.ros.mavenDeploymentRepository : location of the ros maven repository you will publish to
 *
 * It also performs the following actions
 *
 * - checks and maeks sure the maven plugin is running
 * - constructs the sequence of dependant maven repos (local ros maven repos, mavenLocal, external ros maven repo)
 * - configures the uploadArchives for artifact deployment to the local ros maven repo (devel/share/maven)
 */
class RosPlugin implements Plugin<Project> {
  Project project

  def void apply(Project project) {
    this.project = project
    project.apply plugin: "eclipse"
    project.apply plugin: "java"
    project.apply plugin: "maven"
    project.apply plugin: "maven-publish"
    project.apply plugin: "osgi"

    project.sourceCompatibility = 1.6
    project.targetCompatibility = 1.6

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
      mavenLocal()
      maven {
        url "http://repository.springsource.com/maven/bundles/release"
      }
      maven {
        url "http://repository.springsource.com/maven/bundles/external"
      }
      mavenCentral()
    }

    if (project.ros.mavenDeploymentRepository != null &&
        project.ros.mavenDeploymentRepository != "") {
      project.publishing {
        publications {
          mavenJava(MavenPublication) {
            from project.components.java
          }
        }
        repositories {
          maven {
            url project.uri(project.ros.mavenDeploymentRepository)
          }
        }
      }
    }
  }
}

class RosPluginExtension {
  String mavenRepository
  String mavenDeploymentRepository
  List<String> mavenPath
}
