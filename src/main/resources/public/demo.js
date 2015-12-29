function fileToHtml(name,path){
	console.log("[fileToHtml] name:"+name+",path:"+path);
	return "<li><a href='"+path+"'>"+name+"</a>"+"</li>";
}

function folderToHtml(name,path,Children){
	var toReturn = "<li>"+name+"<ul>";
	for (index = 0; index < Children.length; index++) {
		var child = Children[index];	
		if(child.type == "file"){
			toReturn += fileToHtml(child.name,child.path);
		}
		else{
			toReturn += folderToHtml(child.name,child.path,child.children);
		}
	}
	toReturn += "</ul></li>";
	return toReturn; 
}

var filesListAPI = "http://"+location.hostname+":"+location.port+"/files";
$.getJSON(filesListAPI,function(root){
	$('#filesList').html(folderToHtml(root.name,root.path,root.children));
});