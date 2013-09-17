package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.*;

/*
 * Configures java for the ros-android build environment. Pretty elementary right now,
 * just applies the java plugin and defines the jdk compatibility level.
 */
class RosAndroidPlugin implements Plugin<Project> {
    Project project
    
	def void apply(Project project) {
	    this.project = project
        if (!project.plugins.findPlugin('ros')) {
            project.apply(plugin: 'ros')
        }
        project.extensions.create("rosandroid", RosAndroidPluginExtension)
        project.rosandroid.buildToolsVersion = "17"
    }
}

class RosAndroidPluginExtension {
    String buildToolsVersion
}
