package dfs;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class File implements FileOrFolder{
	String name;
	String path;
	byte[] contents;
	public File(String n, String p, byte[] c){
		name = n;
		path = p;
		contents = c;
	}
	public File(String n, String p){
		name = n;
		path = p;
		try{
			contents = Files.readAllBytes(FileSystems.getDefault().getPath(path));
		}
		catch(Exception e){
			System.out.println("Something bad happened!\n" + e);
		}
	}
	public ByteArrayInputStream getByteArrayStream(){
		return new ByteArrayInputStream(contents);
	}
	public byte[] getContents(){
		return contents;
	}
	public String getName(){ return name; }
	public String getPath(){ return path; }
	public JSONObject toJSON(){
		JSONObject jobj = new JSONObject();
		jobj.put("type","file");
		jobj.put("name",name);
		jobj.put("path",path);
		//jobj.put("URI",Folder.getFileSystemPath(path).normalize().toUri());
		jobj.put("fsPath",Folder.getFileSystemPath(path).toString());
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