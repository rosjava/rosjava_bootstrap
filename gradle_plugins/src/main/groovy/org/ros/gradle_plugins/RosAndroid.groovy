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
    project.rosandroid.buildToolsVersion = "28.0.3"

    /********************************************************************** 
     * Publishing - not we're using old style here. Upgrade to maven-publish
     * once they have support: 
     *   https://github.com/rosjava/rosjava_bootstrap/issues/1 
     * This is specifically for releasing and working in a ros workspace.
     **********************************************************************/ 
    project.uploadArchives {
      repositories.mavenDeployer {
        repository(url: 'file://' + project.ros.mavenDeploymentRepository)
      }
    }
    /**********************************************************************
     * Our maven repo 3rd parties are currently incompatible with android
     * junit especially could use a look at - find a compatible version!
     **********************************************************************/
    project.configurations.maybeCreate("compile")
    project.configurations.compile.exclude "group": "junit"
    project.configurations.compile.exclude "group": "xml-apis"
    /**********************************************************************
     * Delay android plugin configuration because that will depend on
     * the subproject's late loading of android or android-library plugin.
     **********************************************************************/
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
