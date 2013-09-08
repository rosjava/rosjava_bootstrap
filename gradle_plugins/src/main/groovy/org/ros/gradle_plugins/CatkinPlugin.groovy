package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec

import org.gradle.api.*;

/*
 * Provides catkin information to the gradle build, defining properties:
 *
 * - project.catkin.workspaces : list of Strings
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
    Project project
    
	def void apply(Project project) {
	    this.project = project
	    /* Create project.catkin.* property extensions */
	    project.extensions.create("catkin", CatkinPluginExtension)
	    project.catkin.packages = [:]
	    project.catkin.workspaces = []
	    project.catkin.workspaces = "$System.env.ROS_PACKAGE_PATH".split(":")
	    /*
	     * Would be more ideal shifting this expensive fileTree operation so
	     * that its' lazily generated (generated only if used), but that's a bit
	     * tricky - maybe by overriding CatkinPluginExtensions.getPackages()? 
	     */
	    project.catkin.workspaces.each { workspace ->
            def manifestTree = project.fileTree(dir: workspace, include: '**/package.xml')
            manifestTree.each { file -> 
                def pkg = new CatkinPackage(file)
                project.catkin.packages.put(pkg.name, pkg)
            }
	    }
	    setTasks()
        println("CatkinPlugin is happy, you should be too.")
    }
    def void setTasks() {
        project.task('catkinPackageInfo') << {
            println("CatkinPlugin is happy, you should be too.")
            println("Catkin Workspaces........." + project.catkin.workspaces)
            println("Catkin Packages")
            project.catkin.packages.each { pkg ->
                print pkg.value.toString()
            }
        }
    }
}
class CatkinPluginExtension {
    List<String> workspaces
    Map<String, CatkinPackage> packages
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
    
    def void generateMessageArtifact(Project p) {
        p.version = version
        p.dependencies.add("compile", 'org.ros.rosjava_bootstrap:message_generator:0.1.0')
        messageDependencies().each { d ->
            p.dependencies.add("compile", p.dependencies.project(path: ':' + d))
        }
        def generatedSourcesDir = "${p.buildDir}/generated-src"
        def generateSourcesTask = p.tasks.create("generateSources", JavaExec)
        generateSourcesTask.description = "Generate sources for " + name
        generateSourcesTask.outputs.dir(p.file(generatedSourcesDir))
        generateSourcesTask.args = new ArrayList<String>([generatedSourcesDir, name])
        generateSourcesTask.classpath = p.configurations.runtime
        generateSourcesTask.main = 'org.ros.internal.message.GenerateInterfaces'
        p.tasks.compileJava.source generateSourcesTask.outputs.files
    }
}

