package dfs;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
//import java.io;
import java.util.Scanner;
import java.io.IOException;

import spark.Spark;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.ServerSocket;

public class Main{
	public static String timeToString(long t){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	return sdf.format(new Date(t));
	}
	public static String timeToString(){
		return timeToString(System.currentTimeMillis());
	}

	public static void copyFolder(Folder f) throws IOException{
		for(FileOrFolder sub : f.getContents().values()){
			if(sub instanceof File){
				File subFile = (File) sub;
				Files.copy(subFile.getByteArrayStream(),FileSystems.getDefault().getPath(f.getPath()+"/"+subFile.getName()), StandardCopyOption.REPLACE_EXISTING);
			}
			else{
				Folder subFolder = (Folder) sub;
				if(!Files.isDirectory(FileSystems.getDefault().getPath( subFolder.getPath() ))){
					Files.createDirectory(FileSystems.getDefault().getPath(subFolder.getPath()));
				}
				copyFolder(subFolder);
			}
		}
	};
	public static void copyFolderWinSafe(Folder f) throws IOException{
		for(FileOrFolder sub : f.getContents().values()){
			Path localPath = Folder.getFileSystemPath(sub.getPath());
			if(sub instanceof File){
				File subFile = (File) sub;
				//Files.copy(subFile.getByteArrayStream(),FileSystems.getDefault().getPath(f.getPath()+"/"+subFile.getName()), StandardCopyOption.REPLACE_EXISTING);
				if(Files.exists(localPath)){
					if ( !Arrays.equals(subFile.getContents(),Files.readAllBytes(localPath)) )
						Files.write(localPath,subFile.getContents());
				} else {
					Files.write(localPath,subFile.getContents());
				}
			}
			else{
				Folder subFolder = (Folder) sub;
				if(!Files.isDirectory(localPath)){
					Files.createDirectory(localPath);
				}
				copyFolderWinSafe(subFolder);
			}
		}
	};

	//debug
	public static void printPart(Part p) throws IOException{
		System.out.println("[printPart] printing part " + p + "{");
		System.out.println("contentType="+p.getContentType());
		System.out.println("Header Names="+p.getHeaderNames());
		for(String hn : p.getHeaderNames()){
			System.out.println("header " + hn + "=" + p.getHeader(hn) + "; " + p.getHeaders(hn));
		}
		System.out.println("name="+p.getName());
		System.out.println("size="+p.getSize());
		System.out.println("submitted file name="+p.getSubmittedFileName());
		System.out.println("Data:\n"+IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8));
		System.out.println("}");
	};

	public static boolean isPortFree(int port){
		boolean result;
		try{
			ServerSocket s = new ServerSocket(port);
			s.close();
			result = true;
		} catch(Exception e) {
			result = false;
		}
		return result;
	}

	public static void main(String[] args) throws IOException{
		Folder root = new Folder(".",".");
		//copyFolder(root);
		Folder.loadFolderFromInternalToFS(Paths.get("."),".");
		System.out.println("Copied root directory! Root : \n" + root);
		//Folder.syncFolder(".",".");
		//Folder.syncFolderWinSafe(Paths.get("."));
		Folder.loadFolderFromFSToInternalAndNotify(".");
		System.out.println("Synced with file directory root! Root : \n" + root);

		
		System.out.println("[Main] Initializing Directory Watching");
		root.initiateDirectoryWatching();

			
		if(isPortFree(4567)){
			Spark.externalStaticFileLocation(".");
			Spark.staticFileLocation("public");
			Spark.get("/files",(req,res)->{
				res.type("application/json");
				return root.toJSON();
			});
			Spark.post("/upload",(req,res)->{
				if (req.raw().getAttribute("org.eclipse.jetty.multipartConfig") == null) {
	 				MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
					req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
				}
				Part file = req.raw().getPart("file");
				Part name = req.raw().getPart("name");
				String filename = file.getSubmittedFileName();
				if(name.getSize() > 0){
					try{
						filename = IOUtils.toString(name.getInputStream(), StandardCharsets.UTF_8);
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				Path filePath = Paths.get(".",filename);
				Files.copy(file.getInputStream(),filePath);
				return "Done!"; //TODO return JSON informing actions taken/not taken
			});
			Spark.get("/delete",(req,res)->{
				if(req.queryParamsValues("df") != null){
					for(String fileIntPath: req.queryParamsValues("df")){
						System.out.println("Spark INFO: Request to DELETE file:"+fileIntPath);
						Folder.deleteFileFromFS(fileIntPath);
					}
				}
				if(req.queryParamsValues("dd") != null){
					for(String dirIntPath: req.queryParamsValues("dd")){
						System.out.println("Spark INFO: Request to DELETE directory:"+dirIntPath);
						Folder.deleteFolderFromFS(dirIntPath);
					}
				}
				return "Done!"; //TODO return JSON informing actions taken/not taken
			});
			System.out.println("Spark INFO : Started Server on port 4567");
		}
	}
}
