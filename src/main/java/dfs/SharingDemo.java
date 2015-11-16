package dfs;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.io.File;

public class SharingDemo{
	public static void copyFolder(Folder f){
		for(FileOrFolder sub : f.getContents().values()){
			if(sub instanceof File){
				File f = (File) sub;
				Files.copy(f.getByteArrayStream(),FileSystems.getDefault().getPath(f.getPath()));
			}
			else{
				Folder subFolder = (Folder) sub;
				Files.createDirectory(FileSystems.getDefault().getPath(subFolder.getPath()));
				copyFolder(subFolder);
			}
		}
	};
	public static void syncFolder(String path, Folder syncDest){
		File top = new File(path);
		for(File sub : top.listFiles()){
			if(!sub.isDirectory()){
				if(!syncDest)
			}
		}
	}
	public static void main(String[] args){
		Folder root = new Folder(".",".");
		copyFolder(root);
		Scanner reader = new Scanner(System.in);
		while(true){
			reader.nextLine();

		};
	}
}