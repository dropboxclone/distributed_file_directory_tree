package dfs;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
//import java.io;
import java.util.Scanner;
import java.io.IOException;

import spark.Spark;

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
	public static void copyFolderWinSafe(Folder f) throws IOException{
		for(FileOrFolder sub : f.getContents().values()){
			Path localPath = Folder.getFileSystemPath(sub.getPath());
			if(sub instanceof File){
				File subFile = (File) sub;
				//Files.copy(subFile.getByteArrayStream(),FileSystems.getDefault().getPath(f.getPath()+"/"+subFile.getName()), StandardCopyOption.REPLACE_EXISTING);
				if(Files.exists(localPath)){
					if ( !Arrays.equals(subFile.getContents(),Files.readAllBytes(localPath)) )
						Files.write(localPath,subFile.getContents());
				} else {
					Files.write(localPath,subFile.getContents());
				}
			}
			else{
				Folder subFolder = (Folder) sub;
				if(!Files.isDirectory(localPath)){
					Files.createDirectory(localPath);
				}
				copyFolderWinSafe(subFolder);
			}
		}
	};
	public static void main(String[] args) throws IOException{
		Folder root = new Folder(".",".");
		//copyFolder(root);
		Folder.loadFolderFromInternalToFS(Paths.get("."),".");
		System.out.println("Copied root directory! Root : \n" + root);
		//Folder.syncFolder(".",".");
		//Folder.syncFolderWinSafe(Paths.get("."));
		Folder.loadFolderFromFSToInternalAndNotify(".");
		System.out.println("Synced with file directory root! Root : \n" + root);

		
		System.out.println("[Main] Initializing Directory Watching");
		root.initiateDirectoryWatching();

		Spark.externalStaticFileLocation(".");
		Spark.staticFileLocation("public");
		Spark.get("/files",(req,res)->{
			res.type("application/json");
			return root.toJSON();
		});
	}
}
