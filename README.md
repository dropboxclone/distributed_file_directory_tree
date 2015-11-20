#Sharing Files Demo
This is a very simple demo to show sharing of files across nodes.

##Usage
###To run the demo
1. Download the [fat jar](https://github.com/dropboxclone/distributed_file_directory_tree/raw/sharing/build/libs/distributed_file_system-all.jar) 
2. Choose some directory whose contents are to be synced. Navigate to this directory in terminal (or command prompt).
3. Run `java -jar <path to fat jar>` with the appropriate path to the downloaded fat jar. For example `java -jar ~/Desktop/distributed_file_system-all.jar` if the jar was downloaded to desktop.
4. Choose a target directory to retrieve the synced contents. Open this directory in a new terminal and again run `java -jar <path to fat jar>`.
You will notice that the contents of the source directory have been synced to the target directory.

**Note** [Java 8+ JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) is required to run the jar. 

##Video Demo
Link - https://youtu.be/n-lrRLQGPTw  
[![Demo Video](http://img.youtube.com/vi/n-lrRLQGPTw/0.jpg)](https://www.youtube.com/watch?v=n-lrRLQGPTw)

##TODO
* Current implementation produces an error, if the target directory has copy of some files already. Fix that.
* Operations in the shared directory tree are not atomic, so conflicts and race conditions may occur. Use hazelcast's locking mechanism to fix this.

**Imp. Note** In this case the shared directory tree has been modified to store the contents of individual files also.
