package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.*;

/*
 * Provides information about the ros workspace.
 *
 * - project.ros.maven : location of local ros maven repository
 * 
 * Use this only once in the root of a multi-project gradle build - it will
 * only generate the properties once and share them this way.
 */
class RosPlugin implements Plugin<Project> {
    Project project
    
	def void apply(Project project) {
	    this.project = project
	    println("RosPlugin")
	    /* Create project.ros.* property extensions */
	    project.extensions.create("ros", RosPluginExtension)
	    project.ros.maven = "$System.env.ROS_MAVEN_DEPLOYMENT_PATH"
        if ( project.ros.maven != 'null' && project.ros.maven != '' ) {
            uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: 'file://' + project.rosMavenDeploymentPath)
                    }
                }
            }
        }
    }
}

class RosPluginExtension {
    String maven
}
