---
layout: docs
title: Building from sources
prev_section: support
next_section: contributing
permalink: /docs/building/
---

Building OWNER from the sources has some advantages. For instance you can get 
the latest features still under development on GitHub master branch.

Building OWNER requires following software being installed and configured in 
your system:

 - [JDK](http://docs.oracle.com/javase/7/docs/webnotes/install/) 1.5 or superior.
 - [Maven](http://maven.apache.org/download.cgi#Installation_Instructions) 3.0.5 
   or superior (Maven 2 should also be ok). 
 - [GIT](http://git-scm.com/book/en/Getting-Started-Installing-Git) any recent version should be ok.

Then follow these steps:

```bash
# Download the sources
$ git clone https://github.com/lviggiano/owner.git owner
$ cd owner
# Compile, execute test, and generate the artifacts
$ mvn install
```

This will build and install OWNER jars in your local maven repository.
At the end of the process, you should find the generated artifacts in the 
`target` subdirectory.

<div class="note">
  <h5>GIT URLs</h5>
  <p>
The above examples uses the https url to clone the GIT repository, alternatively
- if your firewall allows - you can use the GIT native URL that may be 
faster: git://github.com/lviggiano/owner.git.  
  </p>
</div>


Building a specific version
---------------------------

The `git clone` command downloads the full repository with the complete history
on your local computer. That also contains tags for the released versions.

For instance, if I want to build the version 1.0.2

```bash
# as example, this time we use the git:// URL
$ git clone git://github.com/lviggiano/owner.git owner
$ cd owner
# show all available tags
$ git tag -l
owner-1.0
owner-1.0.1
owner-1.0.2
owner-1.0.3
owner-1.0.3.1
owner-1.0.4
$ git checkout owner-1.0.2
HEAD is now at d2e4bbf... [maven-release-plugin] prepare release owner-1.0.2
$ mvn install
```

Please refer to [GIT documentation](http://git-scm.com/documentation) to learn 
how to work with tags.


Running the tests
-----------------

OWNER codebase is very compact and [fully tested][]. 

To execute the tests, you need maven properly installed and configured in your 
system, then run the following command from the project root:

```
$ mvn test
```

  [fully tested]: http://newinstance.it/owner/latest/cobertura/index.html


Continuous Integration 
----------------------

You can access latest builds from 
 [Jenkins](https://aeonbits.ci.cloudbees.com/job/owner-api/) and 
 [Travis](https://travis-ci.org/lviggiano/owner) websites.

Code quality reports
--------------------

You can access latest code quality reports from 
[Sonar](http://sheldon.dyndns.tv:9000/dashboard/index/org.aeonbits.owner:owner)
and [Maven](http://owner.newinstance.it/latest/project-reports.html) 
websites.
