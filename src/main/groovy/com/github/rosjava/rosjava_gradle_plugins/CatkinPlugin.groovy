package com.github.rosjava.rosjava_gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.*;

class CatkinPlugin implements Plugin<Project> {
	def void apply(Project project) {
	    project.ext.ROS_PACKAGE_PATH = "$System.env.ROS_PACKAGE_PATH".split(":")
        project.ext.ROS_PACKAGE_TREES = [] 
	    project.ext.ROS_PACKAGE_PATH.each { rosPackageRoot ->
	        println("Ros Package Root: " + rosPackageRoot)
            def manifestTree = project.fileTree(dir: rosPackageRoot, include: '**/package.xml')
            manifestTree.each { file -> 
                println("File: " + file)
            }
	    }
        println("We are happy, you should be too.")
        project.task('happy') << {
            println "I'll teach your grandmother to suck eggs!"
            println("ROS_PACKAGE_PATH........." + project.ROS_PACKAGE_PATH)
        }
    }
}