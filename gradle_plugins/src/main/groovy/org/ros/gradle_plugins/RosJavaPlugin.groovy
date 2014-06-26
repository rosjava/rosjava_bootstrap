package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.*;

/*
 * Configures java for the ros build environment. Pretty elementary right now,
 * just applies the java plugin and defines the jdk compatibility level.
 */
class RosJavaPlugin implements Plugin<Project> {
    Project project
    
	def void apply(Project project) {
	    this.project = project
        if (!project.plugins.findPlugin('ros')) {
            project.apply(plugin: 'ros')
        }
        if (!project.plugins.findPlugin('java')) {
            project.apply(plugin: 'java')
        }
        if (!project.plugins.findPlugin('maven-publish')) {
            project.apply(plugin: 'maven-publish')
        }
        
        project.sourceCompatibility = 1.6
        project.targetCompatibility = 1.6

        if ( project.ros.mavenDeploymentRepository != 'null' && project.ros.mavenDeploymentRepository != '' ) {
            project.publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from project.components.java
                    }
                }
                repositories {
                    maven {
                       url 'file://' + project.ros.mavenDeploymentRepository
                    }
                }
            }
        }
    }
}

class RosJavaPluginExtension {
    String maven
}
