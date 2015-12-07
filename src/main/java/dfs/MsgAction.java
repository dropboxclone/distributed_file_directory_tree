package dfs;

import java.nio.file.Files;
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
		if(act.getAction() == "add_file" || act.getAction() == "edit_file"){
			//Folder path
			int lastBackSlashIndex = act.getPath().lastIndexOf("/");
			String parentFolderPath = act.getPath().substring(0,lastBackSlashIndex);
			String fileName = act.getPath().substring(lastBackSlashIndex+1);
			Map<String,FileOrFolder> contents = instance.getMap(parentFolderPath);
			File newFile = (File) contents.get(fileName);
			try{
				Files.copy(newFile.getByteArrayStream(),FileSystems.getDefault().getPath(act.getPath()), StandardCopyOption.REPLACE_EXISTING);
			}
			catch(Exception e){
				System.out.println("[MsgAction] Exception e="+e);
			}
		}
		else if(act.getAction() == "delete_file"){
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
		else if(act.getAction() == "create_folder"){
			//TODO : will create conflict incase directory already present
			try{
				Files.createDirectory(FileSystems.getDefault().getPath(act.getPath()));
			}
			catch(Exception e){
				System.out.println("[MsgAction] Exception e="+e);
			}
		}
		else if(act.getAction() == "delete_folder"){
			//java.io.File top = new java.io.File(act.getPath());
			//for(java.io.File sub : top.listFiles()){
			deleteFileorFolderOnDisk(new java.io.File(act.getPath()));
		}
		else if(act.getAction() == "delete_entry"){
			deleteFileorFolderOnDisk(new java.io.File(act.getPath()));
		}
		else{
			//TODO
			System.out.println("[MsgAction] unexpected action="+act);
		}
	}
}