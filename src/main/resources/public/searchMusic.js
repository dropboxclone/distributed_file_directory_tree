function addMusicToList(name,path,fsPath,artist,title){
	var li = document.createElement("li");
	li.id = path;
	var div = document.createElement("div");
	var a = document.createElement("a");
	a.href = fsPath;
	a.innerHTML = name;
	div.appendChild(a);
	div.appendChild(document.createElement("br"));
	var audioPlayer = document.createElement("audio");
	audioPlayer.innerHTML = "Your browser does not support the audio element.";
	audioPlayer.setAttribute("controls","");
	var source = document.createElement("source");
	source.setAttribute("src",fsPath);
	audioPlayer.appendChild(source);
	div.appendChild(audioPlayer);
	li.appendChild(div);
	document.getElementById("musicList").appendChild(li);
};

function handleSearchButtonClick(){
	var queryString = document.getElementById("q").value;
	var musicSearchAPI = "http://"+location.hostname+":"+location.port+"/search/music?q="+queryString;
	$.getJSON(musicSearchAPI,function(res){
		document.getElementById("musicList").innerHTML = "";
		for(var i=0; i<res.length; i++){
			addMusicToList(res[i].name,res[i].path,res[i].fsPath,res[i].artist,res[i]);
		}
	});
};