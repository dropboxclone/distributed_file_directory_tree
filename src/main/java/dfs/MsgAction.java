package dfs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.hazelcast.core.HazelcastInstance;
import java.nio.file.FileSystems;

import com.hazelcast.core.*;

import java.util.Map;
import java.nio.file.StandardCopyOption;

public class MsgAction implements MessageListener<Action>{
	static HazelcastInstance instance;
	public MsgAction(HazelcastInstance i){
		instance = i;
	}

	public static void deleteFileorFolderOnDisk(java.io.File f){
		if(!f.isDirectory())
			f.delete();
		else{
			for(java.io.File sub : f.listFiles())
				deleteFileorFolderOnDisk(sub);
			f.delete();
		}
	}

	public void onMessage(Message<Action> msg){
		Action act = msg.getMessageObject();
		if(act.getAction().equals("add_file") || act.getAction().equals("edit_file")){
			//int lastBackSlashIndex = act.getPath().lastIndexOf("/");
			//String parentFolderPath = act.getPath().substring(0,lastBackSlashIndex);
			//String fileName = act.getPath().substring(lastBackSlashIndex+1);
			String parentFolderPath = Folder.locateParentFolder(Paths.get(act.getPath()));
			String fileName = (new java.io.File(act.getPath())).getName(); 

			Map<String,FileOrFolder> contents = instance.getMap(parentFolderPath);
			File newFile = (File) contents.get(fileName);
			Path newFilePath = Paths.get(act.getPath());
			try{
				if(Files.exists(newFilePath)){
					if(!Arrays.equals(Files.readAllBytes(newFilePath),newFile.getContents())){
						try{
							Files.write(newFilePath,newFile.getContents());
						}
						catch(Exception e){
							System.out.println("[MsgAction] Exception in inner e="+e);
						}
					}
				}
				else{
					Files.write(newFilePath,newFile.getContents());
				}
			}
			catch(Exception e){
				System.out.println("[MsgAction] Exception in outer e="+e);
			}
		}
		else if(act.getAction().equals("delete_file")){
			//todo delete from internal representation
			int lastBackSlashIndex = act.getPath().lastIndexOf("/");
			String parentFolderPath = act.getPath().substring(0,lastBackSlashIndex);
			String fileName = act.getPath().substring(lastBackSlashIndex+1);
			try{
				Files.delete(FileSystems.getDefault().getPath(act.getPath()));
			}
			catch(Exception e){
				System.out.println("[MsgAction] Exception e="+e);
			}
		}
		else if(act.getAction().equals("create_folder")){
			//TODO : will create conflict incase directory already present
			try{
				Files.createDirectory(FileSystems.getDefault().getPath(act.getPath()));
			}
			catch(Exception e){
				System.out.println("[MsgAction] Exception e="+e);
			}
		}
		else if(act.getAction().equals("delete_folder")){
			//java.io.File top = new java.io.File(act.getPath());
			//for(java.io.File sub : top.listFiles()){
			deleteFileorFolderOnDisk(new java.io.File(act.getPath()));
		}
		else if(act.getAction().equals("delete_entry")){
			deleteFileorFolderOnDisk(new java.io.File(act.getPath()));
		}
		else{
			//TODO
			System.out.println("[MsgAction] unexpected action="+act);
		}
	}
}