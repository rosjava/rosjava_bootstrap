Changelog
=========

0.3.3 (2019-01-17)
------------------
  Gradle upgrade to version 4.10.2.
* Adding google to repository list in buildscript.gradle.
* Using ChannelBuffers for int8[] data; fixing testInt8List.
* Adding action generation implementation.
* Add tests for byte arrays.
* Add tests for incomplete initialization and string arrays.
* Add tests for fixed sized arrays of floats.
* Add Bazel build for message_generation.
* Minor fixes.
* Contributors: Arne, Ernesto Corbellini, Juan Ignacio Ubeira, Rodrigo Queiro

0.3.2 (2017-05-09)
------------------
* Bumping message_generation version to kinetic range in CatkinPlugin script for generating new message artifacts properly.
* Contributors: Juan Ignacio Ubeira, Julian Cerruti

0.3.1 (2016-12-28)
------------------
* Switch Gradle target for rosjava libraries to publish
* Switch from Maven Central to jcenter
* Gradle 2.2.1 -> 2.14.1
* Android SDK Build Tools 21.1.2 -> 25.0.2
* Contributors: Daniel Stonier, Julian Cerruti

0.3.0 (2016-12-13)
------------------
* Updates for Kinetic release.

0.2.1 (2015-02-25)
------------------
* minor bugfixes and improvements.
* java source compatibility for java 1.6 -> 1.7
* centralised buildscript for java packages.
* add eclipse and idea plugins for easy ide support.
* Add support of UTF-8!
* update to the latest gradle plugin.
* single interface generator for genjava.
* Fix SSL connection errors with Java 1.7.
* Contributors: Damon Kohler, Daniel Stonier, Mickael Gaillard, talregev

0.1.21 (2014-06-09)
-------------------
* Android gradle plugin 0.9.+->0.11.+ (studio 0.6)
* Android sdk build tools 19.0.3 -> 19.1 (studio 0.6)
* Contributors: Daniel Stonier

0.1.20 (2014-03-20)
-------------------
* Trim maven repository list and backup with maven central.
* Contributors: Daniel Stonier

0.1.19 (2014-03-19)
-------------------
* gradle 1.11, buildTools 19.0.3, gradle android plugin 0.9.+
* remove unused debugging variables
* Contributors: Daniel Stonier

0.1.18 (2014-02-09)
-------------------
* set default maven repo if variable is empty and bugfix dynamic property warnings.
* Contributors: Daniel Stonier

0.1.16 [2013-12-26]
-------------------
* android plugin 0.6.1 -> 0.7.1

0.1.15 [2013-12-26]
-------------------
* message generation now has a single api for official and unofficial releases
* message generation api smart enough to work out internal or external dependencies
* fix dependency problems on non xxx_msg packages - i.e. check for build_depends on message_generation

0.1.14 [2013-12-11]
-------------------
* stop unofficial message generation accidentally picking up older versions of packages.

0.1.13 [2013-12-08]
-------------------
* fix unofficial message package internal/external dependency configuration.

0.1.12 [2013-11-08]
-------------------
* fix single artifact message generation when there is dependencies.

0.1.11 (2013-10-31)
-------------------
* catkin tree generator now takes latest versions only.

0.1.9 (2013-10-31)
------------------
* utilise ROS_MAVEN_REPOSITORY

0.1.8 (2013-10-26)
------------------
* bugfix upgrade version numbers for rosgraph_test_msgs.
* gradle 1.7->1.8 and android_tools->18.1.1
* avoid using .+ ranged dependencies as it breaks the repo

0.1.7 (2013-09-23)
------------------
* use maven-publish plugin for publishing rosjava packages.
* centralise last code snippets from android build.gradle's.

0.1.6 (2013-09-22)
------------------
* disabling osgi.

0.1.5 (2013-09-18)
------------------
* maven deployment path -> maven deployment repository.
* bugfix install location.

0.1.4 (2013-09-17)
------------------
* depend on message generation 0.1.+
* use package.xml version.
* run_depends for the build tools.

0.1.3 (2013-09-17)
------------------
* added excludes to the ros android plugin.

0.1.2 (2013-09-17)
------------------
* ros android plugin added
* gradle wrapper -> 1.7

0.1.1 (2013-09-13)
------------------
* message artifact creation bugfixes.

0.1.0 (2013-09-12)
------------------
* several plugins for sharing of ros gradle logic
* message generation code brought in from rosjava_core

