var connection = null;

async function authorized() {
  resetData();
  await loadData();
}

// following the APP GUIDELINES: https://api.pryv.com/guides/app-guidelines/
const urlParams = new URLSearchParams(window.location.search);
const apiEndpoint = urlParams.get('pryvApiEndpoint');
const serviceInfoUrl = urlParams.get('pryvServiceInfoURL') || 'https://reg.pryv.me/service/info';

var username = null; // will be inialized after AUTHORIZED auth State is received
window.onload = async (event) => {
  if (apiEndpoint != null) { // if apiEndpoint then we are in "View only mode"
    document.getElementById('welcome-message-viewer').style.visibility = 'visible';
    connection = new Pryv.Connection(apiEndpoint);
    let displayUsername = apiEndpoint.split('@')[1];
    if (displayUsername[displayUsername.length - 1] == '/') displayUsername = displayUsername.slice(0,-1);
    document.getElementById('username').innerText = displayUsername;
    authorized();
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
}

async function loadData() {
  const result = await connection.api([{method: 'events.get', params: {limit: 40}}]); // get events from the Pryv.io account
  const events = result[0].events;
  if (! events || events.length === 0) {
    alert('There is no data to show. Use the Collect survey data example first');
    return;
  }
  const contact = document.getElementById('contact');
  contact.style.position = 'absolute';
  // grab contact data
  const contactEmail = document.getElementById('contact-email');
  contactEmail.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('contact-email') && event.type === 'note/txt') {
      contact.style.position = 'relative';
      contactEmail.innerText = event.content;
      contactEmail.style.visibility = 'visible';
      document.getElementById('contact').style.display = '';
      document.getElementById('contact').style.visibility = 'visible';
      break;
    }
  }
  // grab contact data
  const contactName = document.getElementById('contact-name');
  contactName.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('contact-name') && event.type === 'call/name') {
      contactName.innerText = event.content;
      contactName.style.visibility = 'visible';
      document.getElementById('contact').style.display = '';
      document.getElementById('contact').style.visibility = 'visible';
      break;
    }
  }
  // grab contact data
  const contactMobile = document.getElementById('contact-mobile');
  contactMobile.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('contact-mobile') && event.type === 'call/telephone') {
      contactMobile.innerText = event.content;
      contactMobile.style.visibility = 'visible';
      document.getElementById('contact').style.display = '';
      document.getElementById('contact').style.visibility = 'visible';
      break;
    }
  }
  // grab data lists
  const heartDataTable = document.getElementById('blood-pressure-table');
  heartDataTable.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('blood-pressure') && event.type === 'blood-pressure/mmhg-bpm') { // get 'blood-pressure/mmhg-bpm' events from the stream 'blood-pressure'
      addTableEvent(heartDataTable, event, [event.content.systolic + 'mmHg', event.content.diastolic + 'mmHg']);
      heartDataTable.style.visibility = 'visible';
      document.getElementById('data-view').style.display = '';
      document.getElementById('data-view').style.visibility = 'visible';
    }
  }
  // grab heart analysis
  const analysisNote = document.getElementById('analysis-note');
  analysisNote.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('analysis') && event.type === 'note/txt') {
      analysisNote.innerText = event.content;
      analysisNote.style.visibility = 'visible';
      document.getElementById('analysis').style.display = '';
      document.getElementById('analysis').style.visibility = 'visible';
      break;
    }
  }
  // grab diagnosis lists
  const diagnosisDataTable = document.getElementById('diagnosis-table');
  diagnosisDataTable.style.visibility = 'collapse';
  for (const event of events) {
    if (event.streamIds.includes('diagnosis') && event.type === 'note/txt') {
      addTableEvent(diagnosisDataTable, event, [event.content]);
      document.getElementById('diagnosis-view').style.display = '';
      document.getElementById('diagnosis-view').style.visibility = 'visible';
      diagnosisDataTable.style.visibility = 'visible';
    }
  }
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