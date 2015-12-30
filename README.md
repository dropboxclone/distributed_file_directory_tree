#Simple File Sharing Service
Simple tool to share and sync files across machines (similar to dropbox).

#Usage
* Build the jar using `./gradlew shadowJar` (it is created in build/libs) or download it from [releases section](https://github.com/dropboxclone/dropboxclone/releases).
* Run the jar from the directory that you to share or get shared files/folders. E.g. `java -jar ~/dropboxclone/build/libs/dropboxclone-all.jar`. This directory will be shared across all instances of the program.
* **Web Interface** You can access the web-interface by opening `localhost:4567` from a web browser (will work only from the machine running the prgram), OR open `<IP address of machine running>:4567` from any device.
