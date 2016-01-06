package dfs;

import org.json.JSONObject;
import org.json.JSONArray;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.config.Config;
import java.util.Map;
import java.util.Stack;
import java.lang.StringBuilder;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import java.io.InputStream;
import java.nio.file.DirectoryStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
//import java.io;

import com.hazelcast.core.ITopic;

import java.io.IOException;


public class Folder implements FileOrFolder{
	String name;
	String path;
	final static HazelcastInstance instance = Hazelcast.newHazelcastInstance(new Config());
	public static Set<String> dontWatch = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());

	//Map<String,FileOrFolder> contents;

	//ITopic<Action> actions;
	//final static WatchDir watch;

	public void initiateDirectoryWatching() throws IOException{
		ITopic<Action> actions = instance.getTopic("Actions");
		actions.addMessageListener(new MsgAction(instance));
		(new Thread(
			new WatchDir(FileSystems.getDefault().getPath(path),actions)
			)
		).start();
	}

	public Folder(String n, String p){
		name = n;
		path = p;
		Map<String,FileOrFolder> contents = instance.getMap(path);
	}
	public String getName(){ return name; }
	public String getPath(){ return path; }
	public JSONObject toJSON(){
		JSONObject jobj = new JSONObject();
		jobj.put("type","folder");
		jobj.put("name",name);
		jobj.put("path",path);
		//jobj.put("URI",Folder.getFileSystemPath(path).normalize().toUri());
		jobj.put("fsPath",Paths.get(".").relativize(Folder.getFileSystemPath(path)).toString());
		jobj.put("children",new JSONArray());
		//TODO: put locking
		Map<String,FileOrFolder> contents = instance.getMap(path);
		for (FileOrFolder subFileOrFolder : contents.values()) {
			jobj.append("children",subFileOrFolder.toJSON());
		}
		return jobj;
	};

	public String toString(){
		return this.toJSON().toString();
	}

	public boolean createSubFolder(String fname){
		Map<String,FileOrFolder> contents = instance.getMap(path);
		if(contents.containsKey(fname))
			return false;
		FileOrFolder subFolder = new Folder(fname,path+"/"+fname);
		contents.put(fname,subFolder);
		return true;
	}
	public static boolean createSubFolderTo(String destPath,String fname){
		Map<String,FileOrFolder> contents = instance.getMap(destPath);
		if(contents.containsKey(fname))
			return false;
		FileOrFolder subFolder = new Folder(fname,destPath+"/"+fname);
		contents.put(fname,subFolder);
		return true;
	}

	/*
	public boolean createFile(String fname){
		Map<String,FileOrFolder> contents = instance.getMap(path);
		if(contents.containsKey(fname))
			return false;
		File nFile = new File(fname,path+"/"+fname);
		contents.put(fname,nFile);
		return true;
	}
	*/
	public void getFileFromDisk(String fname){
		try{
			byte[] fileContents = Files.readAllBytes(FileSystems.getDefault().getPath(path,fname));
			Map<String,FileOrFolder> folderContents = instance.getMap(path);
			FileOrFolder reqFile = new File(fname,path+"/"+fname,fileContents);
			folderContents.put(fname,reqFile);
		}
		catch(Exception e){
			System.out.println("Something bad happened!\n" + e);
		}
	}

	public static void getFileFromDiskTo(String destPath, String fname){
		try{
			byte[] fileContents = Files.readAllBytes(FileSystems.getDefault().getPath(destPath,fname));
			Map<String,FileOrFolder> folderContents = instance.getMap(destPath);
			folderContents.put(fname,new File(fname,destPath+"/"+fname,fileContents));
		}
		catch(Exception e){
			System.out.println("Something bad happened!\n" + e);
		}
	}

	//utility
	public static String[] getInternalParentPathAndName(String internalPath){
		String[] parentPathAndName = new String[2];
		int indexOfLastBackSlash = internalPath.lastIndexOf("/");
		parentPathAndName[0] = internalPath.substring(0,indexOfLastBackSlash);
		parentPathAndName[1] = internalPath.substring(indexOfLastBackSlash+1);
		return parentPathAndName;
	}

	public static boolean fileExists(String internalPath){
		String[] parentPathAndName = getInternalParentPathAndName(internalPath);
		Map<String,FileOrFolder> parentContents = instance.getMap(parentPathAndName[0]);
		if(!parentContents.containsKey(parentPathAndName[1])){
			return false;
		} else {
			if(parentContents.get(parentPathAndName[1]) instanceof File)
				return true;
			else
				return false;
		}
	};

	//utility
	/*
	public static String getInternalPath(Path fsPath){
		StringBuilder internalParentPath = new StringBuilder(fsPath.toString().length());
		internalParentPath.append(fsPath.getName(0).toString());
		for(int i=1; i<fs.getNameCount(); i++)
			internalParentPath.append("/"+fsPath.getName(i).toString());
		return internalParentPath.toString();
	};
	*/

	//utility
	public static boolean isPresentInInternal(String internalParentPath,String internalName){
		return instance.getMap(internalParentPath).containsKey(internalName);
	}

	//To use
	public static void loadFileFromFSToInternal(Path fsPath, String internalParentPath, String internalFileName){
		InputStream fis = null;
		try{
			fis = Files.newInputStream(fsPath);
			byte[] internalFileContents = IOUtils.toByteArray(fis);
			if(
				!isPresentInInternal(internalParentPath,internalFileName) || 
				!Arrays.equals(
					internalFileContents,
					((File) instance.getMap(internalParentPath).get(internalFileName)).getContents()
				)
			) {
				instance.getMap(internalParentPath).put(internalFileName,
					new File(internalFileName,internalParentPath+"/"+internalFileName,internalFileContents)
				);
			}
		} catch(Exception e){
			System.err.println("Exception " + e + " occured in dfs.Folder.loadFileFromFSToInternal");
			System.err.println("Inputs: fsPath="+fsPath+", internalParentPath="+internalParentPath+",internalFileName="+internalFileName);
			e.printStackTrace();
		} finally{
			if(fis != null){
				try{
					fis.close();
				} catch(Exception e){
					System.err.println("Exception " + e + " occured in dfs.Folder.loadFileFromFSToInternal");
					System.err.println("Inputs: fsPath="+fsPath+", internalParentPath="+internalParentPath+",internalFileName="+internalFileName);
					e.printStackTrace();
				}
			}
		}
	};

	//To use
	public static void loadFileFromFSToInternal(Path fsPath, String internalPath){
		String[] internalParentPathAndName = getInternalParentPathAndName(internalPath);
		loadFileFromFSToInternal(fsPath,internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	//To use
	public static void loadFileFromFSToInternal(Path fsPath){
		loadFileFromFSToInternal(fsPath,getInternalPath(fsPath));
	};

	//To use
	public static void loadFileFromFSToInternal(String internalPath){
		loadFileFromFSToInternal(getFileSystemPath(internalPath),internalPath);
	};

	//To use
	public static void loadFileFromFSToInternal(String internalParentPath, String internalFileName){
		loadFileFromFSToInternal(getFileSystemPath(internalParentPath,internalFileName),internalParentPath,internalFileName);
	}

	//utility
	public static byte[] getContentsFromFS(Path fsPath) throws Exception{
		InputStream fis = Files.newInputStream(fsPath);
		byte[] contents = IOUtils.toByteArray(fis);
		fis.close();
		return contents;
	};

	//utility
	public static boolean isEmptyFSFolder(Path fsPath){
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(fsPath)) {
    		return !dirStream.iterator().hasNext();
    	}
    	catch(Exception e){
    		System.err.println("Exception " + e + " occured in dfs.Folder.isEmptyFSFolder");
			System.err.println("Inputs: fsPath="+fsPath);
			e.printStackTrace();
    	}
    	return false;
	};

	//To use
	public static void loadFileFromInternalToFS(Path fsPath, String internalParentPath, String internalFileName){
		try{
			if(
				!Files.exists(fsPath) ||
				!Arrays.equals(
					getContentsFromFS(fsPath),
					((File) instance.getMap(internalParentPath).get(internalFileName)).getContents()
				)
			) {
				Files.write(fsPath,
					((File) instance.getMap(internalParentPath).get(internalFileName)).getContents()
				);
			}
		} catch(Exception e){
			System.err.println("Exception " + e + " occured in dfs.Folder.loadFileFromInternalToFS");
			System.err.println("Inputs: fsPath="+fsPath+", internalParentPath="+internalParentPath+",internalFileName="+internalFileName);
			e.printStackTrace();
			//System.err.println("Throwing back exception to calling function...");
			//throw e;
		}
	};

	//To use
	public static void loadFileFromInternalToFS(Path fsPath, String internalPath){
		String[] internalParentPathAndName = getInternalParentPathAndName(internalPath);
		loadFileFromInternalToFS(fsPath,internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	//To use
	public static void loadFileFromInternalToFS(Path fsPath){
		loadFileFromInternalToFS(fsPath,getInternalPath(fsPath));
	};

	//To use
	public static void loadFileFromInternalToFS(String internalPath){
		loadFileFromInternalToFS(getFileSystemPath(internalPath),internalPath);
	};

	//To use
	public static void loadFileFromInternalToFS(String internalParentPath, String internalFileName){
		loadFileFromInternalToFS(getFileSystemPath(internalParentPath,internalFileName),internalParentPath,internalFileName);
	};

	//To use
	public static void loadFolderFromFSToInternal(Path fsPath, String internalParentPath, String internalFolderName){
		String internalFolderPath = internalParentPath+"/"+internalFolderName;
		try{
			if(!instance.getMap(internalParentPath).containsKey(internalFolderName)){
				instance.getMap(internalParentPath).put(internalFolderName,
					new Folder(internalFolderName,internalFolderPath)
				);
			}
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(fsPath)){
	        	for(Path content: dirStream){
	        		if(!Files.isDirectory(content))
	        			loadFileFromFSToInternal(
	        				content,
	        				internalFolderPath,
	        				content.getFileName().toString());
	        		else
	        			loadFolderFromFSToInternal(
	        				content,
	        				internalFolderPath,
	        				content.getFileName().toString());
	        	}
	        }
    	} catch (Exception e) {
    		System.err.println("Exception " + e + " occured in dfs.Folder.loadFolderFromFSToInternal");
			System.err.println("Inputs: fsPath="+fsPath+", internalParentPath="+internalParentPath+",internalFolderName="+internalFolderName);
			e.printStackTrace();
    	}
	};

	//To use
	public static void loadFolderFromFSToInternal(Path fsPath, String internalPath){
		String[] internalParentPathAndName = getInternalParentPathAndName(internalPath);
		loadFolderFromFSToInternal(fsPath,internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	//To use
	public static void loadFolderFromFSToInternal(Path fsPath){
		loadFolderFromFSToInternal(fsPath,getInternalPath(fsPath));
	};

	//To use
	public static void loadFolderFromFSToInternal(String internalPath){
		loadFolderFromFSToInternal(getFileSystemPath(internalPath),internalPath);
	};

	//To use
	public static void loadFolderFromFSToInternal(String internalParentPath, String internalFolderName){
		loadFolderFromFSToInternal(getFileSystemPath(internalParentPath,internalFolderName),internalParentPath,internalFolderName);
	};

	//To use
	public static void loadFolderFromInternalToFS(Path fsPath, String internalFolderPath){
		try{
			if(!Files.exists(fsPath))
				Files.createDirectory(fsPath);
			Map<String,FileOrFolder> contents = instance.getMap(internalFolderPath);
			for(String contentName : contents.keySet()){
				if(contents.get(contentName) instanceof File)
					loadFileFromInternalToFS(fsPath.resolve(contentName),internalFolderPath,contentName);
				else
					loadFolderFromInternalToFS(fsPath.resolve(contentName),internalFolderPath+"/"+contentName);
			}
		} catch(Exception e) {
			System.err.println("Exception " + e + " occured in dfs.Folder.loadFolderFromInternalToFS");
			System.err.println("Inputs: fsPath="+fsPath+",internalFolderPath="+internalFolderPath);
			e.printStackTrace();
		}
	};

	//To use
	public static void loadFolderFromInternalToFS(String internalFolderPath){
		loadFolderFromInternalToFS(getFileSystemPath(internalFolderPath),internalFolderPath);
	};

	//To use
	public static void loadFolderFromInternalToFS(Path fsPath){
		loadFolderFromInternalToFS(fsPath,getInternalPath(fsPath));
	}

	//To use
	public static void deleteFileFromInternal(String internalParentPath, String internalFileName){
		instance.getMap(internalParentPath).remove(internalFileName);
	};

	//To use
	public static void deleteFileFromInternal(String internalPath){
		String[] internalParentPathAndName = getInternalParentPathAndName(internalPath);
		deleteFileFromInternal(internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	//To use
	public static void deleteFileFromInternal(Path fsPath){
		deleteFileFromInternal(getInternalPath(fsPath));
	};

	//To use //TODO lock the IMAP while deleting
	public static void deleteFolderFromInternal(String internalParentPath, String internalFolderName){
		String internalFolderPath = internalParentPath + "/" + internalFolderName;
		Map<String,FileOrFolder> folderContents = instance.getMap(internalFolderPath);
		for(String containedFileOrFolderName: folderContents.keySet()){
			if(folderContents.get(containedFileOrFolderName) instanceof File)
				deleteFileFromInternal(internalFolderPath,containedFileOrFolderName);
			else
				deleteFolderFromInternal(internalFolderPath,containedFileOrFolderName);
		}
		instance.getMap(internalParentPath).remove(internalFolderName);
	};

	//To use
	public static void deleteFolderFromInternal(String internalFolderPath){
		String[] internalParentPathAndName = getInternalParentPathAndName(internalFolderPath);
		deleteFolderFromInternal(internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	//To use
	public static void deleteFolderFromInternal(Path fsPath){
		deleteFolderFromInternal(getInternalPath(fsPath));
	};

	public static void deleteFromInternal(String internalParentPath, String internalName){
		FileOrFolder toDelete = (FileOrFolder) instance.getMap(internalParentPath).get(internalName);
		if(toDelete == null) return;
		if(toDelete instanceof File)
			deleteFileFromInternal(internalParentPath,internalName);
		if(toDelete instanceof Folder)
			deleteFolderFromInternal(internalParentPath,internalName);
	};

	public static void deleteFromInternal(String internalPath){
		String[] internalParentPathAndName =  getInternalParentPathAndName(internalPath);
		deleteFromInternal(internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	public static void deleteFromInternal(Path fsPath){
		deleteFromInternal(getInternalPath(fsPath));
	};

	public static void deleteFileFromFS(Path fsPath){
	 	try{
	 		Files.deleteIfExists(fsPath);
	 	} catch(Exception e){
	 		System.err.println("Exception " + e + " occured in dfs.Folder.deleteFileFromFS");
			System.err.println("Inputs: fsPath="+fsPath);
			e.printStackTrace();
	 	}
	};

	public static void deleteFileFromFS(String internalPath){
		deleteFileFromFS(getFileSystemPath(internalPath));
	};

	//To use
	public static void deleteFolderFromFS(Path fsPath){
		try{
			if(Files.exists(fsPath))
				FileUtils.deleteDirectory(fsPath.toFile());
		} catch(Exception e){
			System.err.println("Exception " + e + " occured in dfs.Folder.deleteFolderFromFS");
			System.err.println("Inputs: fsPath="+fsPath);
			e.printStackTrace();
		}
	};

	public static void deleteFolderFromFS(String internalPath){
		deleteFolderFromFS(getFileSystemPath(internalPath));
	};

	public static void deleteFromFS(Path fsPath){
		if(Files.isDirectory(fsPath)){
			deleteFolderFromFS(fsPath);
		} else{
			deleteFileFromFS(fsPath);
		}
	};

	public static void deleteFromFS(String internalPath){
		deleteFromFS(getFileSystemPath(internalPath));
	}

	public static void createEmptyFolderInFS(Path fsPath){
		try{
			if(Files.exists(fsPath)) return;
			Files.createDirectory(fsPath);
		} catch(Exception e){
			System.err.println("Exception " + e + " occured in dfs.Folder.createEmptyFolderInFS");
			System.err.println("Inputs: fsPath="+fsPath);
			e.printStackTrace();
		}
	};

	public static void createEmptyFolderInFS(String internalPath){
		createEmptyFolderInFS(getFileSystemPath(internalPath));
	};

	public static void createEmptyFolderInInternal(String internalParentPath, String internalName){
		instance.getMap(internalParentPath).put(
			internalName,
			new Folder(internalName,internalParentPath+"/"+internalName)
		);
	};

	public static void createEmptyFolderInInternal(String internalPath){
		String[] internalParentPathAndName =  getInternalParentPathAndName(internalPath);
		createEmptyFolderInInternal(internalParentPathAndName[0],internalParentPathAndName[1]);
	};

	public static void createEmptyFolderInInternal(Path fsPath){
		createEmptyFolderInInternal(getInternalPath(fsPath));
	};

	public static String locateParentFolder(Path p){
		java.io.File fileObj = p.toFile();
		java.io.File root = new java.io.File(".");
		Stack<String> parentTrace = new Stack<String>();
		while(!fileObj.getParentFile().equals(root)){
			parentTrace.push(fileObj.getParentFile().getName());
			fileObj = fileObj.getParentFile();
		}
		StringBuilder parentPath = new StringBuilder(".");
		while(!parentTrace.empty()){
			parentPath.append("/" + parentTrace.pop());
		}
		return parentPath.toString();
	}

	public static String getRelFileSystemPath(String internalPath){
		Path fsPath = getFileSystemPath(internalPath);
		return Paths.get(".").relativize(fsPath).toString();
	}

	public static Path getFileSystemPath(String internalPath){
		String[] pathComponents = internalPath.split("/");
		Path r = Paths.get(".");
		for(int i = 1; i < pathComponents.length; i++){
			r = r.resolve(pathComponents[i]);
		}
		return r;
	}

	//utility
	public static Path getFileSystemPath(String internalParentPath,String internalFileName){
		return getFileSystemPath(internalParentPath+"/"+internalFileName);
	};

	public static String getInternalPath(Path fileSystemPath){
		return locateParentFolder(fileSystemPath) + "/" + fileSystemPath.toFile().getName();
	};


    // public static void loadFileFromInternalToFSAndNotify(String internalParentPath, String internalFileName){
    //     Path fsPath = Folder.getFileSystemPath(internalParentPath+"/"+internalFileName);
    //     try{
    //         if(
    //             !Files.exists(fsPath) ||
    //             !Arrays.equals(
    //                 getContentsFromFS(fsPath),
    //                 ((File) instance.getMap(internalParentPath).get(internalFileName)).getContents()
    //             )
    //         ) {
    //             Files.write(fsPath,
    //                 ((File) instance.getMap(internalParentPath).get(internalFileName)).getContents()
    //             );
    //             instance.getTopic("Actions").publish( new Action("add_file",internalParentPath+"/"+internalFileName) );
    //         }
    //     } catch(Exception e){
    //         System.err.println("Exception " + e + " occured in dfs.Folder.loadFileFromInternalToFSAndNotify");
    //         System.err.println("Inputs: internalParentPath="+internalParentPath+",internalFileName="+internalFileName);
    //         e.printStackTrace();
    //     }
    // };


    // public static void loadFolderFromInternalToFSAndNotify(String internalFolderPath){
    //     Path fsPath = Folder.getFileSystemPath(internalFolderPath);
    //     try{
    //         if(!Files.exists(fsPath)) {
    //             Files.createDirectory(fsPath);
    //             instance.getTopic("Actions").publish(new Action("create_empty_folder",internalFolderPath));
    //         }
    //         Map<String,FileOrFolder> contents = instance.getMap(internalFolderPath);
    //         for(String contentName : contents.keySet()){
    //             if(contents.get(contentName) instanceof File) {
    //                 loadFileFromInternalToFSAndNotify(internalFolderPath,contentName);
    //             } else {
    //                 loadFolderFromInternalToFSAndNotify(internalFolderPath+"/"+contentName);
    //             }
    //         }
    //     } catch(Exception e) {
    //         System.err.println("Exception " + e + " occured in dfs.Folder.loadFolderFromInternalToFSAndNotify");
    //         System.err.println("Inputs: internalFolderPath="+internalFolderPath);
    //         e.printStackTrace();
    //     }
    // };

	public static void loadFileFromFSToInternalAndNotify(String internalParentPath, String internalFileName){
		Path fsPath = getFileSystemPath(internalParentPath+"/"+internalFileName);
		InputStream fis = null;
		try{
			fis = Files.newInputStream(fsPath);
			byte[] internalFileContents = IOUtils.toByteArray(fis);
			if(
				!isPresentInInternal(internalParentPath,internalFileName) || 
				!Arrays.equals(
					internalFileContents,
					((File) instance.getMap(internalParentPath).get(internalFileName)).getContents()
				)
			) {
				instance.getMap(internalParentPath).put(internalFileName,
					new File(internalFileName,internalParentPath+"/"+internalFileName,internalFileContents)
				);
				instance.getTopic("Actions").publish( new Action("add_file",internalParentPath+"/"+internalFileName) );
			}
		} catch(Exception e){
			System.err.println("Exception " + e + " occured in dfs.Folder.loadFileFromFSToInternalAndNotify");
			System.err.println("Inputs: internalParentPath="+internalParentPath+",internalFileName="+internalFileName);
			e.printStackTrace();
		} finally{
			if(fis != null){
				try{
					fis.close();
				} catch(Exception e){
					System.err.println("Exception " + e + " occured in dfs.Folder.loadFileFromFSToInternalAndNotify");
					System.err.println("Inputs: internalParentPath="+internalParentPath+",internalFileName="+internalFileName);
					e.printStackTrace();
				}
			}
		}
	};

	public static void loadFolderFromFSToInternalAndNotify(String internalFolderPath){
		Path fsPath = getFileSystemPath(internalFolderPath);
		try{
			if(internalFolderPath != "."){
				String[] internalParentPathAndName = getInternalParentPathAndName(internalFolderPath);
				String internalParentPath = internalParentPathAndName[0];
				String internalFolderName = internalParentPathAndName[1];
				if(!instance.getMap(internalParentPath).containsKey(internalFolderName)){
					instance.getMap(internalParentPath).put(internalFolderName,
						new Folder(internalFolderName,internalFolderPath)
					);
					instance.getTopic("Actions").publish(new Action("create_empty_folder",internalFolderPath));
				}
			}	
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(fsPath)){
	        	for(Path content: dirStream){
	        		if(!Files.isDirectory(content))
	        			loadFileFromFSToInternalAndNotify(
	        				internalFolderPath,
	        				content.getFileName().toString());
	        		else
	        			loadFolderFromFSToInternalAndNotify(
	        				internalFolderPath+"/"+content.getFileName().toString());
	        	}
	        }
    	} catch (Exception e) {
    		System.err.println("Exception " + e + " occured in dfs.Folder.loadFolderFromFSToInternalAndNotify");
			System.err.println("Inputs: internalFolderPath="+internalFolderPath);
			e.printStackTrace();
    	}
	};


	public static void getFileFromDiskToWinSafe(String path){
		java.io.File fileObj = new java.io.File(path);
		java.io.File root = new java.io.File(".");
		//String parentPath = fileObj.getParent();
		String filename = fileObj.getName();
		Stack<String> parentTrace = new Stack<String>();
		while(!fileObj.getParentFile().equals(root)){
			parentTrace.push(fileObj.getParentFile().getName());
			fileObj = fileObj.getParentFile();
		}
		StringBuilder parentPath = new StringBuilder(".");
		while(!parentTrace.empty()){
			parentPath.append("/" + parentTrace.pop());
		}
		getFileFromDiskTo(parentPath.toString(),filename);
	}

	public Map<String,FileOrFolder> getContents(){
		return instance.getMap(path);
	}

	public static void syncFolder(String syncSourceDirectoryPath, String syncDestPath){
		java.io.File top = new java.io.File(syncSourceDirectoryPath);
		for(java.io.File sub : top.listFiles()){
			boolean contains = instance.getMap(syncDestPath).containsKey(sub.getName()); 
			if(!sub.isDirectory()){
				if(!contains)
					getFileFromDiskTo(syncDestPath,sub.getName());
					//syncDest.getFileFromDisk(sub.getName());
			}
			else{
				if(!contains){
					//syncDest.createSubFolder(sub.getName());
					createSubFolderTo(syncDestPath,sub.getName());
				}
				syncFolder(sub.getPath(),syncDestPath+"/"+sub.getName());
			}
		}
	}

	public static void syncFolderWinSafe(Path folderPath){
		for(java.io.File sub : folderPath.toFile().listFiles()){
			String parentPath = locateParentFolder(sub.toPath());
			Map<String,FileOrFolder> parentDirectoryMap = instance.getMap(parentPath);
			boolean isAlreadyPresent = parentDirectoryMap.containsKey(sub.getName());
			if(!sub.isDirectory()){
				try{
					if(isAlreadyPresent){
						File cloudversion = (File) parentDirectoryMap.get(sub.getName());
						if ( !Arrays.equals(cloudversion.getContents(),Files.readAllBytes(sub.toPath())) )
							Files.write(sub.toPath(),cloudversion.getContents());
					} else {
						parentDirectoryMap.put(sub.getName(),new File(sub.getName(),parentPath + "/" + sub.getName(),Files.readAllBytes(sub.toPath())));
					}
				} catch(Exception e){
					System.out.println("Something bad happened.. Exception = " + e);
				}
			} else {
				if(!isAlreadyPresent){
					parentDirectoryMap.put(sub.getName(),new Folder(sub.getName(),parentPath+"/"+sub.getName()));
				}
				syncFolderWinSafe(sub.toPath());
			}
		}
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