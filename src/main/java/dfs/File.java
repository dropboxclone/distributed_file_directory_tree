package dfs;

import org.json.JSONObject;

public class File implements FileOrFolder{
	String name;
	String path;
	public File(String n, String p){
		name = n;
		path = p;
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