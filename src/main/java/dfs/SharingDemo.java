package dfs;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;
//import java.io;
import java.util.Scanner;
import java.io.IOException;

public class SharingDemo{
	public static void copyFolder(Folder f) throws IOException{
		for(FileOrFolder sub : f.getContents().values()){
			if(sub instanceof File){
				File subFile = (File) sub;
				Files.copy(subFile.getByteArrayStream(),FileSystems.getDefault().getPath(f.getPath()+"/"+subFile.getName()), StandardCopyOption.REPLACE_EXISTING);
			}
			else{
				Folder subFolder = (Folder) sub;
				if(!Files.isDirectory(FileSystems.getDefault().getPath( subFolder.getPath() ))){
					Files.createDirectory(FileSystems.getDefault().getPath(subFolder.getPath()));
				}
				copyFolder(subFolder);
			}
		}
	};
	// public static void syncFolder(String path, Folder syncDest){
	// 	io.File top = new io.File(path);
	// 	for(io.File sub : top.listFiles()){
	// 		boolean contains = syncDest.getContents().containsKey(sub.getName()); 
	// 		if(!sub.isDirectory()){
	// 			if(!contains)
	// 				syncDest.getFileFromDisk(sub.getName());
	// 		}
	// 		else{
	// 			if(!contains)
	// 				syncDest.createSubFolder(sub.getName());
	// 			syncFolder(sub.getPath(),)
	// 		}
	// 	}
	// }
	public static void main(String[] args) throws IOException{
		Folder root = new Folder(".",".");
		copyFolder(root);
		System.out.println("Copied root directory! Root : \n" + root);
		Folder.syncFolder(".",".");
		System.out.println("Synced with file directory root! Root : \n" + root);

		
		System.out.println("[Main] Initializing Directory Watching");
		root.initiateDirectoryWatching();

		// Scanner reader = new Scanner(System.in);
		// while(true){
		// 	reader.nextLine();
		// 	Folder.syncFolder(".",".");
		// }
	}
}
