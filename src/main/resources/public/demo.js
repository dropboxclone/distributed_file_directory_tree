function fileToHtml(name,fsPath,parentID){
	var fileElement = document.createElement('li');
	fileElement.id = fsPath;
	fileElement.className = "file";
	var fileElementLink = document.createElement('a');
	fileElementLink.id = "URI:"+fsPath;
	fileElementLink.href = fsPath;
	var fileElementName = document.createElement('div');
	fileElementName.id = "name:"+fsPath;
	fileElementName.innerHTML = name;
	fileElementLink.appendChild(fileElementName);
	fileElement.appendChild(fileElementLink);
	document.getElementById(parentID).appendChild(fileElement);
}

function folderToHtml(name,path,Children,parentID){
	var folderElement = document.createElement('li');
	folderElement.id = path;
	folderElement.className = "folder";
	document.getElementById(parentID).appendChild(folderElement);
	var folderElementName = document.createElement('div');
	folderElementName.id = "name:"+path;
	folderElementName.innerHTML = name;
	folderElement.appendChild(folderElementName);
	var folderElementChildren = document.createElement('ul');
	folderElementChildren.id = "children:"+path;
	folderElement.appendChild(folderElementChildren);
	for (var index = 0; index < Children.length; index++) {
		var child = Children[index];
		if(child.type == "file"){
			fileToHtml(child.name,child.fsPath,"children:"+path);
		}
		else{
			folderToHtml(child.name,child.path,child.children,"children:"+path);
		}
	}
}

var filesListAPI = "http://"+location.hostname+":"+location.port+"/files";
$.getJSON(filesListAPI,function(root){
	folderToHtml(root.name,root.path,root.children,"filesList");
});


//Delete Utilities
function createCheckBoxAroundNameFolder(intPath){
	var nameDiv = document.getElementById("name:"+intPath);
	nameDiv.innerHTML = "<label><input type='checkbox' name='dd' value='"+intPath+"' id='delBtn:"+intPath+"' >"+nameDiv.innerHTML+"</label>";
	nameDiv.getElementsByTagName('input')[0].onclick =function(){handleFolderCheckboxClick(intPath)};
}

function createCheckBoxAroundNameFile(intPath){
	var nameDiv = document.getElementById("name:"+intPath);
	nameDiv.innerHTML = "<label><input type='checkbox' name='df' value='"+intPath+"'>"+nameDiv.innerHTML+"</label>";
}

function handleFolderCheckboxClick(dirIntPath){
	var element = document.getElementById('delBtn:'+dirIntPath);
	var childrenInputBoxes = document.getElementById('children:'+dirIntPath).getElementsByTagName('input');
	for(var i=0; i<childrenInputBoxes.length; i++){
		childrenInputBoxes[i].checked = element.checked;
	}
	if(dirIntPath == "."){
		element.checked = false;
	}
};


function createCheckBoxesRecursively(dirIntPath){
	createCheckBoxAroundNameFolder(dirIntPath);
	var childrenList = document.getElementById("children:"+dirIntPath).children;
	for(var i=0; i<childrenList.length; i++){
		var child = childrenList[i];
		if(child.className=="file"){
			createCheckBoxAroundNameFile(child.id);
		} else {
			createCheckBoxesRecursively(child.id);
		}
	}
}

function createWrappingForm(elementId){
	var element = document.getElementById(elementId);
	var parent = element.parentNode;
	var wrappingForm = document.createElement("form");
	wrappingForm.action = "/delete";
	wrappingForm.method = "get";
	parent.replaceChild(wrappingForm,element);
	wrappingForm.appendChild(element);
	var submitButton = document.createElement("input");
	submitButton.type="submit";
	submitButton.value="Delete Selected";
	wrappingForm.appendChild(submitButton);
}

function handleDeleteButtonClick(){
	document.getElementById("fileUploadDiv").remove();
	document.getElementById("fileDeleteDiv").remove();
	document.getElementById("actionTitle").innerHTML = "Delete Files";
	var back = document.createElement("a");
	back.href="http://"+location.hostname+":"+location.port;
	back.innerHTML = "Back";
	document.getElementById("actionsContainer").appendChild(back);
	createWrappingForm("filesList");
	createCheckBoxesRecursively(".");
}
