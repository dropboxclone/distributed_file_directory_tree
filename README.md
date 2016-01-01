#Simple File Sharing Service
Simple tool to share and sync files across machines (similar to dropbox).

##Usage
* Build the fat jar using `./gradlew shadowJar` (it is created in build/libs) or download the latest version from [releases section](https://github.com/dropboxclone/dropboxclone/releases).
* Run the jar from the directory that you to share or get shared files/folders. E.g. `java -jar ~/dropboxclone/build/libs/dropboxclone-all.jar`. This directory will be shared across all instances of the program.

##TODO
###Important
* Add tests
###Optimize
* Use hashing to identify file change.
* Only send diffs over the network.
 - Use [JGit](https://github.com/eclipse/jgit) or some other kind of version control system.
* Do not store files (especially large ones) in Hazelcast shared memory. Consider transferring files via sockets.
 ###Features
* Select path while uploading files in web-interface.
* Delete files in web-interface.
* Music streaming support in web-interface.
###Other
* Continuous integration support like [Travis CI](https://travis-ci.org/).
* Consider deployment. Perhaps [Heroku](https://www.heroku.com/) or Google App Engine?
