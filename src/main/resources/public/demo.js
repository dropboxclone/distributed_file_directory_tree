function fileToHtml(name,URI){
	return "<li><a href='"+URI+"'>"+name+"</a>"+"</li>";
}

function folderToHtml(name,Children){
	var toReturn = "<li>"+name+"<ul>";
	for (index = 0; index < Children.length; index++) {
		var child = Children[index];	
		if(child.type == "file"){
			toReturn += fileToHtml(child.name,child.URI);
		}
		else{
			toReturn += folderToHtml(child.name,child.children);
		}
	}
	toReturn += "</ul></li>";
	return toReturn; 
}

var filesListAPI = "http://"+location.hostname+":"+location.port+"/files";
$.getJSON(filesListAPI,function(root){
	$('#filesList').html(folderToHtml(root.name,root.children));
});