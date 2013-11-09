package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec

import org.gradle.api.*;

/*
 * Provides catkin information to the gradle build, defining properties:
 *
 * - project.catkin.pkg : information about this package
 * - project.catkin.workspaces : list of Strings
 * - project.catkin.tree.generate() : create the pkgs dictionary
 * - project.catkin.tree.pkgs : dictionary of CatkinPackage objects
 * 
 * The latter can be iterated over for information:
 *
 * project.catkin.tree.pkgs.each { pair ->
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
	    project.catkin.workspaces = []
	    project.catkin.workspaces = "$System.env.ROS_PACKAGE_PATH".split(":")
        project.catkin.tree = new CatkinPackages(project, project.catkin.workspaces)
        def packageXml = project.file('package.xml')
        if ( !packageXml.exists() ) {
            def parentDirectoryName = file.getParentFile().getParent();
            packageXml = project.file('package.xml')
        }
        if (packageXml != null) {
            project.catkin.pkg = new CatkinPackage(packageXml)
        }
	    setTasks()
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
    CatkinPackage pkg
    List<String> workspaces
    CatkinPackages tree
}
class CatkinPackages {
    def Map<String, CatkinPackage> pkgs
    def List<String> workspaces
    def Project project
    
    def CatkinPackages(Project project, List<String> workspaces) {
        this.project = project
        this.workspaces = workspaces
        this.pkgs = [:]
    }
    
    def generate() {
        if ( this.pkgs.size() == 0 ) {
            this.workspaces.each { workspace ->
                def manifestTree = project.fileTree(dir: workspace, include: '**/package.xml')
                manifestTree.each { file -> 
                    def pkg = new CatkinPackage(file)
                    if(this.pkgs.containsKey(pkg.name)) {
                        if(this.pkgs[pkg.name].version < pkg.version) {
                            println("Catkin generate tree: replacing older version of " + pkg.name + "[" + this.pkgs[pkg.name].version + "->" + pkg.version + "]") 
                            this.pkgs[pkg.name] = pkg
                        }
                    } else {
                        this.pkgs.put(pkg.name, pkg)
                    }
                }
            }
        }
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
    
    def void generateMessageArtifact(Project p) {
        p.version = version
        p.dependencies.add("compile", 'org.ros.rosjava_bootstrap:message_generation:[0.2,0.3)')
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

    def void generateUnofficialMessageArtifact(Project p) {
        /* Couple of constraints here:
             1) maven group forced to org.ros.rosjava_messages to that all message artifact
                dependencies are easily found.
             2) Open ended dependency range (takes the latest in ROS_PACKAGE_PATH) since we
                don't know the artifact versions the user really wants.
        */
        p.version = version
        p.group = 'org.ros.rosjava_messages'
        p.dependencies.add("compile", 'org.ros.rosjava_bootstrap:message_generation:[0.2,0.3)')
        messageDependencies().each { d ->
            p.dependencies.add("compile", 'org.ros.rosjava_messages:' + d + ':[0.1,)')
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

    /*
     * Hack to work around for rosjava_test_msgs - look in a subfolder for the
     * msgs and name the artifact by the subfolder name/version.
     */
    def void generateMessageArtifactInSubFolder(Project p, String subfolderName, List<String> dependencies) {
        // p.version = version use the subfolder's project version
        p.dependencies.add("compile", 'org.ros.rosjava_bootstrap:message_generation:[0.2,0.3)')
        dependencies.each { d ->
            p.dependencies.add("compile", p.dependencies.project(path: ':' + d))
        }
        def generatedSourcesDir = "${p.buildDir}/generated-src"
        def generateSourcesTask = p.tasks.create("generateSources", JavaExec)
        generateSourcesTask.description = "Generate sources for " + name + "/" + subfolderName
        generateSourcesTask.outputs.dir(p.file(generatedSourcesDir))
        generateSourcesTask.args = new ArrayList<String>([generatedSourcesDir, subfolderName])
        generateSourcesTask.classpath = p.configurations.runtime
        generateSourcesTask.main = 'org.ros.internal.message.GenerateInterfaces'
        p.tasks.compileJava.source generateSourcesTask.outputs.files
    }
}

