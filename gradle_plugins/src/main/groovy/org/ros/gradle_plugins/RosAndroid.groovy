package org.ros.gradle_plugins;

import org.gradle.api.Project
import org.gradle.api.Plugin
import java.util.HashMap

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
        project.apply(plugin: 'android')
        project.extensions.create("rosandroid", RosAndroidPluginExtension)
        project.rosandroid.buildToolsVersion = "17"
        project.configurations.create('compile')
        /* 
         * Our maven repo 3rd parties are currently incompatible with android
         * junit especially could use a look at - find a compatible version!
         */
        def excludes = new HashMap<String, String>()
        excludes.put('group', 'junit')
        excludes.put('group', 'xml-apis')
        project.configurations['compile'].exclude(excludes)
    }
}

class RosAndroidPluginExtension {
    String buildToolsVersion
}
