package dfs;

import org.json.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.config.Config;
import java.util.Map;

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
		
	}

	public Map<String,FileOrFolder> getContents(){
		return instance.getMap(path);
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