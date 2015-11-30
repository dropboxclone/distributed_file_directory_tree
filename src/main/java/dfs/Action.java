package dfs;

import java.io.Serializable;

public class Action implements Serializable{
	public String action;
	public String path;

	public String(String a, String p){
		action = a;
		path = p;
	}
	public String getAction(){return action;}
	public String getPath(){return path;}
}