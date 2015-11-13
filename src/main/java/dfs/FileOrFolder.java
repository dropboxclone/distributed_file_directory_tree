package dfs;

import org.json.JSONObject;
import java.io.Serializable;

public interface FileOrFolder extends Serializable{
	String getName();
	String getPath();
	JSONObject toJSON();
	String toString();
};