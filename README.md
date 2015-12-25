#Messaging
Implemented messaging between nodes.
Java directory watch service tracks the root folder for changes. Whenever some event (create new file, edit existing files, delete folder, etc.) takes place, then it triggers the node to send a message to all other nodes about the change and they update themselves too.
In this version, the new files/ edits are pushed to the hazelcast shared map and all nodes retrieve it from there.