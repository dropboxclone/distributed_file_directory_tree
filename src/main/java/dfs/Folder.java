package dfs;

import org.json.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.config.Config;
import java.util.Map;
import java.util.Stack;
import java.lang.StringBuilder;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

//import java.io;

import com.hazelcast.core.ITopic;

import java.io.IOException;


public class Folder implements FileOrFolder{
	String name;
	String path;
	final static HazelcastInstance instance = Hazelcast.newHazelcastInstance(new Config());
	//Map<String,FileOrFolder> contents;

	//ITopic<Action> actions;
	//final static WatchDir watch;

	public void initiateDirectoryWatching() throws IOException{
		ITopic<Action> actions = instance.getTopic("Actions");
		actions.addMessageListener(new MsgAction(instance));
		(new Thread(
			new WatchDir(FileSystems.getDefault().getPath(path),actions)
			)
		).start();
	}

	public Folder(String n, String p){
		name = n;
		path = p;
		Map<String,FileOrFolder> contents = instance.getMap(path);
	}
	public String getName(){ return name; }
	public String getPath(){ return path; }
	public JSONObject toJSON(){
		JSONObject jobj = new JSONObject();
		jobj.put("type","folder");
		jobj.put("name",name);
		jobj.put("path",path);
		//TODO: put locking
		Map<String,FileOrFolder> contents = instance.getMap(path);
		for (FileOrFolder subFileOrFolder : contents.values()) {
			jobj.append("children",subFileOrFolder.toJSON());
		}
		return jobj;
	};

	public String toString(){
		return this.toJSON().toString();
	}

	public boolean createSubFolder(String fname){
		Map<String,FileOrFolder> contents = instance.getMap(path);
		if(contents.containsKey(fname))
			return false;
		FileOrFolder subFolder = new Folder(fname,path+"/"+fname);
		contents.put(fname,subFolder);
		return true;
	}
	public static boolean createSubFolderTo(String destPath,String fname){
		Map<String,FileOrFolder> contents = instance.getMap(destPath);
		if(contents.containsKey(fname))
			return false;
		FileOrFolder subFolder = new Folder(fname,destPath+"/"+fname);
		contents.put(fname,subFolder);
		return true;
	}

	/*
	public boolean createFile(String fname){
		Map<String,FileOrFolder> contents = instance.getMap(path);
		if(contents.containsKey(fname))
			return false;
		File nFile = new File(fname,path+"/"+fname);
		contents.put(fname,nFile);
		return true;
	}
	*/
	public void getFileFromDisk(String fname){
		try{
			byte[] fileContents = Files.readAllBytes(FileSystems.getDefault().getPath(path,fname));
			Map<String,FileOrFolder> folderContents = instance.getMap(path);
			FileOrFolder reqFile = new File(fname,path+"/"+fname,fileContents);
			folderContents.put(fname,reqFile);
		}
		catch(Exception e){
			System.out.println("Something bad happened!\n" + e);
		}
	}

	public static void getFileFromDiskTo(String destPath, String fname){
		try{
			byte[] fileContents = Files.readAllBytes(FileSystems.getDefault().getPath(destPath,fname));
			Map<String,FileOrFolder> folderContents = instance.getMap(destPath);
			folderContents.put(fname,new File(fname,destPath+"/"+fname,fileContents));
		}
		catch(Exception e){
			System.out.println("Something bad happened!\n" + e);
		}
	}

	public static String locateParentFolder(Path p){
		java.io.File fileObj = p.toFile();
		java.io.File root = new java.io.File(".");
		Stack<String> parentTrace = new Stack<String>();
		while(!fileObj.getParentFile().equals(root)){
			parentTrace.push(fileObj.getParentFile().getName());
			fileObj = fileObj.getParentFile();
		}
		StringBuilder parentPath = new StringBuilder(".");
		while(!parentTrace.empty()){
			parentPath.append("/" + parentTrace.pop());
		}
		return parentPath.toString();
	}

	public static Path getFileSystemPath(String internalPath){
		String[] pathComponents = internalPath.split("/");
		Path r = Paths.get(".");
		for(int i = 1; i < pathComponents.length; i++){
			r = r.resolve(pathComponents[i]);
		}
		return r;
	}

	public static String getInternalPath(Path fileSystemPath){
		return locateParentFolder(fileSystemPath) + "/" + fileSystemPath.toFile().getName();
	}

	public static void getFileFromDiskToWinSafe(String path){
		java.io.File fileObj = new java.io.File(path);
		java.io.File root = new java.io.File(".");
		//String parentPath = fileObj.getParent();
		String filename = fileObj.getName();
		Stack<String> parentTrace = new Stack<String>();
		while(!fileObj.getParentFile().equals(root)){
			parentTrace.push(fileObj.getParentFile().getName());
			fileObj = fileObj.getParentFile();
		}
		StringBuilder parentPath = new StringBuilder(".");
		while(!parentTrace.empty()){
			parentPath.append("/" + parentTrace.pop());
		}
		getFileFromDiskTo(parentPath.toString(),filename);
	}

	public Map<String,FileOrFolder> getContents(){
		return instance.getMap(path);
	}

	public static void syncFolder(String syncSourceDirectoryPath, String syncDestPath){
		java.io.File top = new java.io.File(syncSourceDirectoryPath);
		for(java.io.File sub : top.listFiles()){
			boolean contains = instance.getMap(syncDestPath).containsKey(sub.getName()); 
			if(!sub.isDirectory()){
				if(!contains)
					getFileFromDiskTo(syncDestPath,sub.getName());
					//syncDest.getFileFromDisk(sub.getName());
			}
			else{
				if(!contains){
					//syncDest.createSubFolder(sub.getName());
					createSubFolderTo(syncDestPath,sub.getName());
				}
				syncFolder(sub.getPath(),syncDestPath+"/"+sub.getName());
			}
		}
	}

	public static void syncFolderWinSafe(Path folderPath){
		for(java.io.File sub : folderPath.toFile().listFiles()){
			String parentPath = locateParentFolder(sub.toPath());
			Map<String,FileOrFolder> parentDirectoryMap = instance.getMap(parentPath);
			boolean isAlreadyPresent = parentDirectoryMap.containsKey(sub.getName());
			if(!sub.isDirectory()){
				try{
					if(isAlreadyPresent){
						File cloudversion = (File) parentDirectoryMap.get(sub.getName());
						if ( !Arrays.equals(cloudversion.getContents(),Files.readAllBytes(sub.toPath())) )
							Files.write(sub.toPath(),cloudversion.getContents());
					} else {
						parentDirectoryMap.put(sub.getName(),new File(sub.getName(),parentPath + "/" + sub.getName(),Files.readAllBytes(sub.toPath())));
					}
				} catch(Exception e){
					System.out.println("Something bad happened.. Exception = " + e);
				}
			} else {
				if(!isAlreadyPresent){
					parentDirectoryMap.put(sub.getName(),new Folder(sub.getName(),parentPath+"/"+sub.getName()));
				}
				syncFolderWinSafe(sub.toPath());
			}
		}
	}


	// public boolean equals(Object obj){
	// 	if(obj == this)
	// 		return true;
	// 	else if(obj == null || obj.getClass() != this.getClass()) 
	// 		return false;
	// 	else{
	// 		return this.getPath() == ((FileOrFolder) obj).getPath();
	// 	}
	// }
}