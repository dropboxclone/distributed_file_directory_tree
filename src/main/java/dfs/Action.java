package dfs;

import java.io.Serializable;

public class Action implements Serializable{
	public String action;
	public String path;

	public Action(String a, String p){
		action = a;
		path = p;
	}
	public String getAction(){return action;}
	public String getPath(){return path;}
	public void setPath(String p){path = p;}
	public String toString(){
		return "{action:"+action+",path:"+path+"}";
	}
}