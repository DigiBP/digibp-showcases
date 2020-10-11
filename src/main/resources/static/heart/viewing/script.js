async function authorized() {
  resetData();
  document.getElementById('please-login').style.visibility = 'hidden';
  document.getElementById('data-view').style.display = '';
  document.getElementById('data-view').style.visibility = 'visible';
  document.getElementById('analysis').style.display = '';
  document.getElementById('analysis').style.visibility = 'visible';
  document.getElementById('diagnosis-view').style.display = '';
  document.getElementById('diagnosis-view').style.visibility = 'visible';
  document.getElementById('sharing-view').style.display = '';
  document.getElementById('sharing-view').style.visibility = 'visible';
  await loadData();
}

function showLoginMessage() {
  resetData();
  document.getElementById('please-login').style.visibility = 'visible';
  document.getElementById('data-view').style.display = 'none';
  document.getElementById('data-view').style.visibility = 'hidden';
  document.getElementById('analysis').style.display = 'none';
  document.getElementById('analysis').style.visibility = 'hidden';
  document.getElementById('diagnosis-view').style.display = 'none';
  document.getElementById('diagnosis-view').style.visibility = 'hidden';
  document.getElementById('sharing-view').style.display = 'none';
  document.getElementById('sharing-view').style.visibility = 'hidden';
}

// following the APP GUIDELINES: https://api.pryv.com/guides/app-guidelines/
const urlParams = new URLSearchParams(window.location.search);
const apiEndpoint = urlParams.get('pryvApiEndpoint');
const serviceInfoUrl = urlParams.get('pryvServiceInfoURL') || 'https://reg.pryv.me/service/info';

var service = null; // will be initialized after setupAuth;
var username = null; // will be inialized after AUTHORIZED auth State is received
window.onload = async (event) => {
  
  if (apiEndpoint != null) { // if apiEndpoint then we are in "View only mode"
    document.getElementById('welcome-message-mme').style.visibility = 'hidden';
    document.getElementById('welcome-message-viewer').style.visibility = 'visible';
    connection = new Pryv.Connection(apiEndpoint);
    authorized();
    document.getElementById('sharing-view').style.display = 'none';
    document.getElementById('sharing-view').style.visibility = 'hidden';
  } else { // we propose a login
    service = await Pryv.Browser.setupAuth(authSettings, serviceInfoUrl);

    // register "Create" sharing button event listener 
    document.getElementById('create-sharing').addEventListener("click", createSharing);
  }
};

function resetTable(tableId) {
  var tableBody = document.querySelector('#' + tableId + ' tbody');
  if (tableBody)
    while (tableBody.firstChild) tableBody.removeChild(tableBody.firstChild);
}

function resetData() {
  resetTable('blood-pressure-table');
  resetTable('diagnosis-table');
  resetTable('sharings-table');
}

async function loadData() {
  const result = await connection.api([{method: 'events.get', params: {limit: 40}}]); // get events from the Pryv.io account
  const events = result[0].events;
  if (! events || events.length === 0) {
    alert('There is no data to show. Use the Collect survey data example first');
    return;
  }
  // grab data lists
  const heartDataTable = document.getElementById('blood-pressure-table');
  heartDataTable.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('blood-pressure') && event.type === 'blood-pressure/mmhg-bpm') { // get 'blood-pressure/mmhg-bpm' events from the stream 'blood-pressure'
      addTableEvent(heartDataTable, event, [event.content.systolic + 'mmHg', event.content.diastolic + 'mmHg']);
      heartDataTable.style.visibility = 'visible';
    }
  }
  // grab heart analysis
  const analysisNote = document.getElementById('analysis-note');
  analysisNote.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('analysis') && event.type === 'note/txt') {
      analysisNote.innerText = event.content;
      analysisNote.style.visibility = 'visible';
      break;
    }
  }
  // grab diagnosis lists
  const diagnosisDataTable = document.getElementById('diagnosis-table');
  diagnosisDataTable.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('diagnosis') && event.type === 'note/txt') {
      addTableEvent(diagnosisDataTable, event, [event.content]);
      diagnosisDataTable.style.visibility = 'visible';
    }
  }
  if (apiEndpoint == null) // display sharings only when logged-in
    updateSharings();
}
function addTableEvent(table, event, items) {
  function pad(n) { return n < 10 ? '0' + n : n }
  const date = new Date(event.time * 1000); // add date of the fetched events
  const dateText = date.getFullYear() + '/' + (date.getMonth() + 1) + '/' + date.getDate() + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes());

  const row = table.insertRow(-1);
  row.insertCell(-1).innerHTML = dateText;
  for (const item of items) {
    row.insertCell(-1).innerHTML = item;
  }
};

// ----- Sharings

async function updateSharings() {
  const result = await connection.api([ // https://github.com/pryv/lib-js#api-calls
    { 
      method: 'accesses.get', // get accesses of the data: https://api.pryv.com/reference/#get-accesses
      params: {}
    }
  ]); 
  const sharingTable = document.getElementById('sharings-table');
  const accesses = result[0].accesses;
  if (! accesses || accesses.length === 0) {
    return;
  }
  resetTable('sharings-table'); // empty list
  for (const access of accesses) {
    await addListAccess(sharingTable, access);
  }
}

async function createSharing() {
  const isBloodPressureChecked = document.getElementById('check-blood-pressure').checked;
  const isAnalysisChecked = document.getElementById('check-analysis').checked;
  const isDiagnosisChecked = document.getElementById('check-diagnosis').checked;
  if (! isAnalysisChecked && ! isBloodPressureChecked && ! isDiagnosisChecked) { 
    alert('Check at least one of the streams');
    return;
  }
  const name = document.getElementById('sharing-name').value.trim();
  if (! name || name === '') {
    alert('Enter a name for your sharing');
    return;
  }
  // set permissions
  const permissions = [];
  if (isBloodPressureChecked) permissions.push({streamId: 'blood-pressure', level: 'read'});
  if (isAnalysisChecked) permissions.push({ streamId: 'analysis', level: 'read' });
  if (isDiagnosisChecked) permissions.push({streamId: 'diagnosis', level: 'read'});

  const res = await connection.api([ // https://github.com/pryv/lib-js#api-calls
    { 
      method: 'accesses.create', // creates the selected access: https://api.pryv.com/reference/#create-access
      params: {
        name: name,
        permissions: permissions
      }
  }]);
  const error = res[0].error;
  if (error != null) {
    displayError(error);
    return;
  }
  updateSharings();

  function displayError(error) {
    let message = error.message;
    if (error.id.includes('forbidden')) {
      message = `${error.message} Please enter data first.`
    }
    alert(JSON.stringify(message, null, 2));
  }
}

async function addListAccess(table, access) { // add permissions to the sharings table
  
  const permissions = [];
  for (const permission of access.permissions) permissions.push(permission.streamId);
  const apiEndpoint = await service.apiEndpointFor(username, access.token);

  const sharingURL = window.location.href.split('?')[0] + '../sharing/?pryvApiEndpoint=' + apiEndpoint;
  const sharingLink = '<a href="' + sharingURL + '" target="_new"> open </a>';

  const emailSubject = encodeURIComponent('Access my ' + permissions.join(', ') + ' data');
  const emailBody = encodeURIComponent('Hello,\n\nClick on the following link ' + sharingURL);

  const emailLink = '<a href="mailto:?subject=' + emailSubject + '&body=' + emailBody + '"> email </a>';

  const deleteLink = '<a href="" onclick="javascript:deleteSharing(\'' + access.id + '\');return false;">' + access.name + '</a>';

  const row = table.insertRow(-1);
  row.insertCell(-1).innerHTML = deleteLink;
  row.insertCell(-1).innerHTML = permissions.join(', ');
  row.insertCell(-1).innerHTML = sharingLink;
  row.insertCell(-1).innerHTML = emailLink;
};

async function deleteSharing(accessId) {
  if (! confirm('delete?')) return;
  await connection.api([ // https://github.com/pryv/lib-js#api-calls
    {
      method: 'accesses.delete', // deletes the selected access: https://api.pryv.com/reference/#delete-access 
      params: {id: accessId}
  }
]); 
  updateSharings();
}