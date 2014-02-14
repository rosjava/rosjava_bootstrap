package org.ros.gradle_plugins;

import org.gradle.api.Project
import org.gradle.api.Plugin
import java.util.HashMap

/**
 * Configures ROS on Android build environment.
 */
class RosAndroidPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.apply plugin: "ros"
    project.extensions.create("rosandroid", RosAndroidPluginExtension)
    project.rosandroid.buildToolsVersion = "18.1.1"

    //Find the android plugin
    project.buildscript {
      repositories {
        mavenCentral()
      }
      dependencies {
        classpath "com.android.tools.build:gradle:0.7.1"
      }
    }

    // Note that we're using old style here. Upgrade to maven-publish once they
    // have support: https://github.com/rosjava/rosjava_bootstrap/issues/1
    project.uploadArchives {
      repositories.mavenDeployer {
        repository(url: uri(project.ros.mavenDeploymentRepository))
      }
    }

    // Our Maven repo 2rd parties are currently incompatible with Android JUnit.
    project.configurations.create("compile")
    project.configurations.compile.exclude "group": "junit"
    project.configurations.compile.exclude "group": "xml-apis"

    // Delay android plugin configuration because that will depend on the
    // subproject's late loading of android or android-library plugin.
    project.afterEvaluate {
      project.android {
        buildToolsVersion project.rosandroid.buildToolsVersion
      }
    }
  }
}

class RosAndroidPluginExtension {
  String buildToolsVersion
}
