^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Changelog for package rosjava_bootstrap
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

0.1.12 [2013-11-08]
------------------
* fix single artifact message generation when there is dependencies.

0.1.11 (2013-10-31)
------------------
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

