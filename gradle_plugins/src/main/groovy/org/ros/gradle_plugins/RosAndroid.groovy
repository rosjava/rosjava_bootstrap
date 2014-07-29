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
    project.rosandroid.buildToolsVersion = "20"

    // Our Maven repo 2rd parties are currently incompatible with Android JUnit.
    project.configurations.maybeCreate("compile")
    project.configurations.compile.exclude "group": "junit"
    project.configurations.compile.exclude "group": "xml-apis"

    // Delay android plugin configuration because that will depend on the
    // subproject's late loading of android or android-library plugin.
    project.afterEvaluate {
      project.android {
        buildToolsVersion project.rosandroid.buildToolsVersion
        lintOptions {
          disable "InvalidPackage"
        }
      }
    }
  }
}

class RosAndroidPluginExtension {
  String buildToolsVersion
}
