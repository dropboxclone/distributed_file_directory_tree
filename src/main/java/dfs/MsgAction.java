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
		System.out.println("[MsgAction][Debug] Received new action="+act);
		if(act.getAction().equals("add_file") || act.getAction().equals("edit_file")){
			//Folder path
			int lastBackSlashIndex = act.getPath().lastIndexOf("/");
			String parentFolderPath = act.getPath().substring(0,lastBackSlashIndex);
			String fileName = act.getPath().substring(lastBackSlashIndex+1);
			Map<String,FileOrFolder> contents = instance.getMap(parentFolderPath);
			System.out.println("[MsgAction-if] parentFolderPath="+parentFolderPath+",fileName="+fileName+"\nKeys in parentFolderPath="+instance.getMap(parentFolderPath).keySet());
			File newFile = (File) contents.get(fileName);

			Path newFilePath = Paths.get(act.getPath());
			System.out.println("[MsgAction-add_file] error not in statement [Path newFilePath = Paths.get(act.getPath());]");
			try{
				System.out.println("[MsgAction-add_file] Check if file already exits="+Files.exists(newFilePath));
				if(Files.exists(newFilePath)){
					System.out.println("[MsgAction-add_file] error not in statement [if(Files.exists(newFilePath)){]");
					if(!Arrays.equals(Files.readAllBytes(newFilePath),newFile.getContents())){
						try{
							//Files.copy(newFile.getByteArrayStream(),FileSystems.getDefault().getPath(act.getPath()), StandardCopyOption.REPLACE_EXISTING);
							Files.write(newFilePath,newFile.getContents()); //todo
						}
						catch(Exception e){
							System.out.println("[MsgAction] Exception in inner e="+e);
						}
					}
					else{
						System.out.println("[MsgAction][Debug] No action on action="+act+" since it already exists with the same value");
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