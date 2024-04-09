const uri = 'http://localhost/GolfMaster/service/shot-data.jsp?player=louisju';
let todos = [];

function getItems() 
{
	fetch(uri)
		.then(response => response.json())
		.then(data => _displayItems(data))
		.catch(error => console.error('Unable to get items.', error));
}

function addItem() 
{
	const addNameTextbox = document.getElementById('add-name');

	const item = {
		isComplete: false,
		name: addNameTextbox.value.trim()
	};

	fetch(uri, {
		method: 'POST',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(item)
	})
		.then(response => response.json())
		.then(() => {
			getItems();
			addNameTextbox.value = '';
		})
		.catch(error => console.error('Unable to add item.', error));
}

function deleteItem(id) {
	fetch(`${uri}/${id}`, {
		method: 'DELETE'
	})
		.then(() => getItems())
		.catch(error => console.error('Unable to delete item.', error));
}

function displayEditForm(id) {
	const item = todos.find(item => item.id === id);

	document.getElementById('edit-name').value = item.name;
	document.getElementById('edit-id').value = item.id;
	document.getElementById('edit-isComplete').checked = item.isComplete;
	document.getElementById('editForm').style.display = 'block';
}

function updateItem() {
	const itemId = document.getElementById('edit-id').value;
	const item = {
		id: parseInt(itemId, 10),
		isComplete: document.getElementById('edit-isComplete').checked,
		name: document.getElementById('edit-name').value.trim()
	};

	fetch(`${uri}/${itemId}`, {
		method: 'PUT',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(item)
	})
		.then(() => getItems())
		.catch(error => console.error('Unable to update item.', error));

	closeInput();

	return false;
}

function closeInput() {
	document.getElementById('editForm').style.display = 'none';
}

function _displayCount(itemCount) {
	const name = (itemCount === 1) ? 'to-do' : 'to-dos';

	document.getElementById('counter').innerText = `${itemCount} ${name}`;
}

function _displayItems(data) {
	const tBody = document.getElementById('todos');
	tBody.innerHTML = '';

	_displayCount(data.length);

	const button = document.createElement('button');

	data.forEach(item => {
		let isCompleteCheckbox = document.createElement('input');
		isCompleteCheckbox.type = 'checkbox';
		isCompleteCheckbox.disabled = true;
		isCompleteCheckbox.checked = item.isComplete;

		let editButton = button.cloneNode(false);
		editButton.innerText = 'Edit';
		editButton.setAttribute('onclick', `displayEditForm(${item.id})`);

		let deleteButton = button.cloneNode(false);
		deleteButton.innerText = 'Delete';
		deleteButton.setAttribute('onclick', `deleteItem(${item.id})`);

		let tr = tBody.insertRow();

		let td1 = tr.insertCell(0);
		td1.appendChild(isCompleteCheckbox);

		let td2 = tr.insertCell(1);
		let textNode = document.createTextNode(item.name);
		td2.appendChild(textNode);

		let td3 = tr.insertCell(2);
		td3.appendChild(editButton);

		let td4 = tr.insertCell(3);
		td4.appendChild(deleteButton);
	});

	todos = data;
}

function send()
{
    var ItemJSON;

    //ItemJSON = '[  {    "Id": 1,    "ProductID": "1",    "Quantity": 1,  },  {    "Id": 1,    "ProductID": "2",    "Quantity": 2,  }]';
	ItemJSON = 'player=louisju';
    //URL = "https://testrestapi.com/additems?var=" + urlvariable;  //Your URL
	URL = "http://localhost:8080/GolfMaster/service/shot-data.jsp?player=louisju";
    var xmlhttp = new XMLHttpRequest();
    //xmlhttp.onreadystatechange = callbackFunction(xmlhttp);
    xmlhttp.open("GET", URL, false);
    //xmlhttp.setRequestHeader("Content-Type", "application/json");
	//xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    //xmlhttp.setRequestHeader('Authorization', 'Basic ' + window.btoa('apiusername:apiuserpassword')); //in prod, you should encrypt user name and password and provide encrypted keys here instead 
    xmlhttp.onreadystatechange = callbackFunction(xmlhttp);
    xmlhttp.send(ItemJSON);
    //alert(xmlhttp.responseText);
    document.getElementById("div").innerHTML = xmlhttp.statusText + ":" + xmlhttp.status + "<BR><textarea rows='100' cols='100'>" + xmlhttp.responseText + "</textarea>";
	
	var resultobj = JSON.parse(xmlhttp.responseText);
	var dataArray = resultobj.result;
	var listplater = "";
	
	for(var i = 0; i < dataArray.length; ++i)
	{
		listplater = listplater + "<BR>" + dataArray[i].Player;
	}
	document.getElementById("player").innerHTML = listplater;
}

function callbackFunction(xmlhttp) 
{
    //alert(xmlhttp.responseXML);
}