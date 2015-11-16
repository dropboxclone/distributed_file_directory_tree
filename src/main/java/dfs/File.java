package dfs;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.io.ByteArrayInputStream;

public class File implements FileOrFolder{
	String name;
	String path;
	byte[] contents;
	public File(String n, String p){
		name = n;
		path = p;
		contents = Files.readAllBytes(FileSystems.getDefault().getPath(path));
	}
	public ByteArrayInputStream getByteArrayStream(){
		return new ByteArrayInputStream(contents);
	}
	public String getName(){ return name; }
	public String getPath(){ return path; }
	public JSONObject toJSON(){
		JSONObject jobj = new JSONObject();
		jobj.put("type","file");
		jobj.put("name",name);
		jobj.put("path",path);
		return jobj;
	}
	public String toString(){
		return name;
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