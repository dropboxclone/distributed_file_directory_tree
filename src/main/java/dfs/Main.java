package dfs;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;
//import java.io;
import java.util.Scanner;
import java.io.IOException;

public class Main{
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
	public static void main(String[] args) throws IOException{
		Folder root = new Folder(".",".");
		copyFolder(root);
		System.out.println("Copied root directory! Root : \n" + root);
		Folder.syncFolder(".",".");
		System.out.println("Synced with file directory root! Root : \n" + root);

		
		System.out.println("[Main] Initializing Directory Watching");
		root.initiateDirectoryWatching();
	}
}
