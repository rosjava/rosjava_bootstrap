package com.github.rosjava.rosjava_gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.*;

/*
 * Provides catkin information to the gradle build, defining properties:
 *
 * - project.catkin.rosPackagePath : list of Strings
 * - project.catkin.packages : dictionary of CatkinPackage objects
 * 
 * The latter can be iterated over for information:
 *
 * project.catkin.packages.each { pair ->
 *     pkg = pair.value
 *     println pkg.name
 *     println pkg.version
 *     pkg.dependencies.each { d ->
 *         println d
 *     }
 *     // filtered list of *_msg dependencies.
 *     pkg.messageDependencies().each { d ->
 *         println d
 *     }
 * }
 * 
 * Use this only once in the root of a multi-project gradle build - it will
 * only generate the properties once and share them this way.
 */
class CatkinPlugin implements Plugin<Project> {
    /* 
     * Possibly should check for existence of these properties and 
     * be lazy if they're already defined.
     */
	def void apply(Project project) {
	    project.extensions.create("catkin", CatkinPluginExtension)
	    project.catkin.packages = [:]
	    project.catkin.rosPackagePath = []
	    project.catkin.rosPackagePath = "$System.env.ROS_PACKAGE_PATH".split(":")
	    project.catkin.rosPackagePath.each { rosPackageRoot ->
            def manifestTree = project.fileTree(dir: rosPackageRoot, include: '**/package.xml')
            manifestTree.each { file -> 
                def pkg = new CatkinPackage(file)
                project.catkin.packages.put(pkg.name, pkg)
            }
	    }
        println("CatkinPlugin is happy, you should be too.")
        project.task('catkinPackageInfo') << {
            println("CatkinPlugin is happy, you should be too.")
            println("rosPackagePath........." + project.catkin.rosPackagePath)
            println("Catkin Packages")
            project.catkin.packages.each { pkg ->
                print pkg.value.toString()
            }
        }
    }
}

class CatkinPluginExtension {
    Map<String, CatkinPackage> packages
    List<String> rosPackagePath
}

/*
 * Use this to establish methods that can be used by the project.
 * Currently don't have any.
 */
class CatkinPluginConvention {
    private Project project
    public CatkinPluginConvention(Project project) {
        this.project = project
    }
}

class CatkinPackage {
    def name
    def version
    def dependencies
    
    def CatkinPackage(File packageXmlFilename) {
        def packageXml = new XmlParser().parse(packageXmlFilename)
        name = packageXml.name.text()
        version = packageXml.version.text()
        dependencies = []
        packageXml.build_depend.each { d ->
            dependencies.add(d.text())
        }
    }
    def String toString() {
        def out = new String()
        out += name + "\n"
        out += "  version: " + version + "\n"
        out += "  dependencies:" + "\n"
        dependencies.each { d ->
            out += "    " + d + "\n"
        }
        return out
    }
    /*
     * Find and annotate a list of package package dependencies.
     * Useful for message artifact generation).
     *
     * @return List<String> : dependencies (package name strings)  
     */
    def List<String> messageDependencies() {
        List<String> msgDependencies = []
        dependencies.each { d ->
            if ( d.contains("_msgs") ) {
                msgDependencies.add(d)
            }
        }
        return msgDependencies
    }
}

