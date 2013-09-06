package com.github.rosjava.rosjava_gradle_plugins.catkin.gradle;

import org.gradle.api.*;

public class CatkinPlugin implements Plugin {
    def void apply(Project project) {
        //c4rTask task has been defined below.
        project.task('catkin') << {
            println "Hi from catkin plugin!"
        }
    }
}