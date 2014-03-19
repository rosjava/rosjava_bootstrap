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
        project.extensions.create("rosandroid", RosAndroidPluginExtension)
        project.rosandroid.buildToolsVersion = "19.0.3"
        /*********************************************************************
         * Find the android plugin
         *********************************************************************/ 
        project.buildscript {
            repositories {
                mavenCentral()
            }
            dependencies {
                classpath 'com.android.tools.build:gradle:0.9.+'
            }
        }
        /********************************************************************** 
         * Publishing - not we're using old style here. Upgrade to maven-publish
         * once they have support: 
         *   https://github.com/rosjava/rosjava_bootstrap/issues/1 
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
        project.configurations.create('compile')
        def excludes = new HashMap<String, String>()
        excludes.put('group', 'junit')
        excludes.put('group', 'xml-apis')
        project.configurations['compile'].exclude(excludes)
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
