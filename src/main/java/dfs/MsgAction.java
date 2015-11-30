package dfs;

import java.nio.file.Files;
import com.hazelcast.core.HazelcastInstance;
import java.nio.file.FileSystems;

public class MsgAction implements MessageListener<Action>{
	//final static HazelcastInstance instance;
	public MsgAction(HazelcastInstance i){
		instance = i;
	}
	public void onMessage(Message<Action> msg){
		Action act = msg.getMessageObject();
		if(act.getAction() == "add_file" || act.action == "edit_file"){
			//Folder path
			int lastBackSlashIndex = act.getPath().lastIndexOf("/");
			String parentFolderPath = action.getPath().substring(0,lastBackSlashIndex);
			String fileName = action.getPath().substring(i+1);
			File newFile = h.getMap(parentFolderPath).get(fileName);
			Files.copy(newFile.getByteArrayStream(),FileSystems.getDefault().getPath(act.getPath()), StandardCopyOption.REPLACE_EXISTING);
		}
		else if(act.getAction() == "delete_file"){
			int lastBackSlashIndex = act.getPath().lastIndexOf("/");
			String parentFolderPath = action.getPath().substring(0,lastBackSlashIndex);
			String fileName = action.getPath().substring(i+1);
			Files.delete(FileSystems.getDefault().getPath(act.getPath());
		}
		else if(act.getAction() == "create_folder"){
			//TODO : will create conflict incase directory already present
			Files.createDirectory(FileSystems.getDefault().getPath(act.getPath()));
		}
		else if(act.getAction() == "delete_folder"){
			java.io.File top = new java.io.File(act.getPath());
			//TODO
		}
	}
}