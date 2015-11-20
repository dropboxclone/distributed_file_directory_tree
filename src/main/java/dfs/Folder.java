package dfs;

import org.json.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.config.Config;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.FileSystems;
//import java.io;

public class Folder implements FileOrFolder{
	String name;
	String path;
	final static HazelcastInstance instance = Hazelcast.newHazelcastInstance(new Config());
	//Map<String,FileOrFolder> contents;
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