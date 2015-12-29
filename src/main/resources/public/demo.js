function fileToHtml(name,fsPath,parentID){
	document.getElementById(parentID).innerHTML += "<li><a href='"+fsPath+"'>"+name+"</a>"+"</li>";
}

function folderToHtml(name,path,Children,parentID){
	document.getElementById(parentID).innerHTML += '<li>'+name+'<ul id="'+path+'"></ul></li>';
	for (var index = 0; index < Children.length; index++) {
		var child = Children[index];
		if(child.type == "file"){
			fileToHtml(child.name,child.fsPath,path);
		}
		else{
			folderToHtml(child.name,child.path,child.children,path);
		}
	}
}

var filesListAPI = "http://"+location.hostname+":"+location.port+"/files";
$.getJSON(filesListAPI,function(root){
	folderToHtml(root.name,root.path,root.children,"filesList");
});