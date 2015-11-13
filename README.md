#Shared Directory Tree

##What we intend to do?
We have implemented a data structure to store and share the directory structure of the shared folder in our dropbox clone.  
**Note** This will contain only list of files and not the original files themselves.  
The idea is - whenever a new file or folder (s) is added to the shared folder, they are to be listed in this directory tree, so that at any moment this tree contains the list of all files/folders shared till now. And then when a new node joins in, it traverses this tree and requests each file from the already present nodes.

##Implementation
Each folder has a unique path.  
So for every folder we create a shared map (Hazelcast IMap uniquely identified by folder's path) having its contents which can be sub-folders/files. Names of these sub-folders/files are used as keys for this map, and the corresponding values are reference to sub-folder/file.

##Usage
Create the root folder object using `Folder root = new Folder(".",".")`
And now you access to shared root directory (assuming every uses the same naming convention for root **"."** and root path **"."**). Now you can do operations like create a sub-folder or print it etc (*TODO* other operations like deleting a folder etc are also to be implemented).
###To run the demo
`./gradlew run`  
Also run multiple instances to verify network discovery and sharing of tree.

##TODO
* Use locking to avoid race conditions (if any)
* More functionality
	* Delete Files / Sub Folders
* Traversal
* Beautifully print the directory tree (at present the JSON representation of the tree is printed)