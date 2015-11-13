#Shared Directory Tree

##What we intend to do?
We have implemented a data structure to store and share the directory structure of the shared folder in our dropbox clone.
**Note** This will contain only the list of the files and not the original files themselves.
The idea is - whenever a new file or folder (s) is added to the shared folder, they are to be listed in this directory tree, so that at any moment this tree contains the list of all files/folders shared till now. And then when a new node joins in, it traverses this tree and requests the each file from the already present nodes.

##Usage
Create the root folder object using `Folder root = new Folder(".",".")`
And now you access to shared root directory (assuming every uses the same naming convention **.** and path **.**). Now you can do operations like create a sub-folder or print it etc(*TODO* other operations like deleting a folder etc are also to be implemented).
###To run the demo
`./gradlew run`

##TODO
* Use locking to avoid race conditions (if any)
* More functionality
	* Delete Files / Sub Folders
* Traversal