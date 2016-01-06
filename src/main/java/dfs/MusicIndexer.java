package dfs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FilenameUtils;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.ID3v1;

class MusicMetadata{
	private String track;
	private String artist;
	private String title;
	private String album;
	private String year;
	private String genre;
	private String genreDescription;
	private String comment;
	private String name;
	private MusicMetadata(){
		track = "";
		artist = "";
		title = "";
		album = "";
		year = "";
		genre = "";
		genreDescription = "";
		comment = "";
	};
	MusicMetadata(File m){
		this();
		name = m.getName();
		try{
			Mp3File musicFile = new Mp3File(m.getPath());
			if(musicFile.hasId3v1Tag()){
				ID3v1 id3v1Tag = musicFile.getId3v1Tag();
				track = id3v1Tag.getTrack();
				artist = id3v1Tag.getArtist();
				title = id3v1Tag.getTitle();
				album = id3v1Tag.getAlbum();
				year = id3v1Tag.getYear();
				genre = Integer.toString(id3v1Tag.getGenre());
				genreDescription = id3v1Tag.getGenreDescription();
				comment = id3v1Tag.getComment();
			}
		} catch(Exception e) {
			System.err.println("Exception " + e + " occured in dfs.MusicMetadata.MusicMetadata");
			System.err.println("Inputs: file m="+m + " ,path="+m.getPath());
			e.printStackTrace();
		}
	}
	public String toString(){
		return String.format("Mp3:%s{track=%s,artist=%s,title=%s,album=%s,year=%s,genre=%s,genreDescription=%s,comment=%s}",name,track,artist,title,album,year,genre,genreDescription,comment);
	}
};

public class MusicIndexer extends Thread{
	Folder rootDir;
	Map<File,MusicMetadata> index;
	Queue<Folder> pathToBeIndexed;

	MusicIndexer(Folder r){
		rootDir = r;
		index = new ConcurrentHashMap<File,MusicMetadata>();
		pathToBeIndexed = new ConcurrentLinkedQueue<Folder>();
		this.setPriority(Thread.MIN_PRIORITY);
	}

	private static boolean isMusic(File f){
		return FilenameUtils.getExtension(f.getName()) == "mp3";
	}

	private static boolean isMusic(String fname){
		return FilenameUtils.getExtension(fname) == "mp3";
	}

	private void updateIndex(File f){
		index.put(f,new MusicMetadata(f));
	};

	private void index(Folder dir){
		for(FileOrFolder content : dir.getContents().values()){
			if(content instanceof File){
				if(isMusic((File) content)){
					updateIndex((File) content);
					System.out.println("MusicIndexer INFO: Indexing path="+content.getPath()+", name=content.getName()\nMetadata="+index.get((File) content)); //debug
				}
			} else {
				pathToBeIndexed.add((Folder) content);
			}
		}
	};

	@Override
	public void run(){
		for(;;){
			if(pathToBeIndexed.isEmpty())
				pathToBeIndexed.add(rootDir);
			Folder currentDir = pathToBeIndexed.poll();
			if(currentDir != null){
				index(currentDir);
			}
		}
	}
}
