package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.StopActionException

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
 *   pkg = pair.value
 *   println pkg.name
 *   println pkg.version
 *   pkg.dependencies.each { d ->
 *     println d
 *   }
 *   // filtered list of *_msg dependencies.
 *   pkg.getMessageDependencies().each { d ->
 *     println d
 *   }
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
            project.catkin.pkg = new CatkinPackage(project, packageXml)
        }
        project.catkin.tree.generate()

	    setTasks()
    }
    def void setTasks() {
        project.task('catkinPackageInfo').doLast {
            println("CatkinPlugin is happy, you should be too.")
            println("Catkin Workspaces........." + project.catkin.workspaces)
            println("Catkin Packages")
            project.catkin.tree.pkgs.each { pkg ->
                println(pkg.value.toString())
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

  Map<String, CatkinPackage> pkgs
  List<String> workspaces
  Project project

  CatkinPackages(Project project, List<String> workspaces) {
    this.project = project
    this.workspaces = workspaces
    pkgs = [:]
  }

  void generate() {
    if (pkgs.size() == 0) {
      workspaces.each { workspace ->
        def manifestTree = project.fileTree(dir: workspace,
                                            include: "**/package.xml")
        manifestTree.each { file -> 
          def pkg = new CatkinPackage(project, file)
          if(this.pkgs.containsKey(pkg.name)) {
            if(this.pkgs[pkg.name].version < pkg.version) {
              println("Catkin generate tree: replacing older version of " + pkg.name + "[" + this.pkgs[pkg.name].version + "->" + pkg.version + "]") 
              pkgs[pkg.name] = pkg
            }
          } else {
            pkgs.put(pkg.name, pkg)
          }
        }
      }
    }
  }

  Boolean isMessagePackage(String package_name) {
    def pkg
    def result = false
    try {
      pkg = this.pkgs[package_name]
      /* println("    Name: " + pkg.name + "-" + pkg.version) */
      /* println("    Dep-dependencies: " + pkg.dependencies) */
      pkg.dependencies.each { d ->
        if ( d.equalsIgnoreCase("message_generation") ) {
          result = true
        }
      }
    } catch (NullPointerException e) {
      /* Not a catkin package dependency (e.g. boost), ignore */
      result = false
    }
    return result
  }

  void generateMessageArtifact(Project project, String package_name) {
    def pkg = this.pkgs[package_name]
    project.version = pkg.version
    /* println("Artifact: " + pkg.name + "-" + pkg.version) */
    project.dependencies.add("compile", 'org.ros.rosjava_bootstrap:message_generation:[0.3,0.4)')
    Set<String> messageDependencies = pkg.getMessageDependencies()
    messageDependencies.each { d ->
      if ( project.getParent().getChildProjects().containsKey(d) ) {
        /* println("  Internal: " + d) */
        project.dependencies.add("compile", project.dependencies.project(path: ':' + d))
      } else {
        /* println("  External: " + d) */
        project.dependencies.add("compile", 'org.ros.rosjava_messages:' + d + ':[0.0,)')
      }
    }
    def generatedSourcesDir = "${project.buildDir}/generated-src"
    def generateSourcesTask = project.tasks.create("generateSources", JavaExec)
    generateSourcesTask.description = "Generate sources for " + pkg.name
    generateSourcesTask.outputs.dir(project.file(generatedSourcesDir))
    /* generateSourcesTask.args = new ArrayList<String>([generatedSourcesDir, pkg.name]) */
    generateSourcesTask.args = new ArrayList<String>([generatedSourcesDir, '--package-path=' + pkg.directory, pkg.name])
    generateSourcesTask.classpath = project.configurations.runtime
    generateSourcesTask.main = "org.ros.internal.message.GenerateInterfaces"
    project.tasks.compileJava.source generateSourcesTask.outputs.files
  }
}

class CatkinPackage {
  Project project
  String name
  String version
  Set<String> dependencies
  String directory

  CatkinPackage(Project project, File packageXmlFilename) {
    this.project = project
    /* println "Loading " + packageXmlFilename */
    def packageXml = new XmlParser().parse(packageXmlFilename)
    directory = packageXmlFilename.parent
    name = packageXml.name.text()
    version = packageXml.version.text()
    dependencies = packageXml.build_depend.collect{ it.text() }
  }

  String toString() { "${name} ${version} ${dependencies}" }

  Set<String> getTransitiveDependencies(Collection<String> dependencies) {
    Set<String> result = [];
    dependencies.each {
      if (project.catkin.tree.pkgs.containsKey(it)) {
        result.add(it)
        result.addAll(getTransitiveDependencies(
            project.catkin.tree.pkgs[it].dependencies))
      }
    }
    return result
  }

  Set<String> getMessageDependencies() {
    getTransitiveDependencies(dependencies).findAll {
      project.catkin.tree.pkgs.containsKey(it) &&
      project.catkin.tree.pkgs[it].dependencies.contains("message_generation")
    } as Set
  }

}

