async function authorized() {
  document.getElementById('please-login').style.visibility = 'hidden';
  document.getElementById('form').style.visibility = 'visible';
}

async function showLoginMessage() {
  document.getElementById('please-login').style.visibility = 'visible';
  document.getElementById('form').style.visibility = 'hidden';
}

window.onload = (event) => {
  document.getElementById('submit-button').addEventListener("click", submitForm);

  // following the APP GUIDELINES: https://api.pryv.com/guides/app-guidelines/
  const serviceInfoUrl = Pryv.Browser.serviceInfoFromUrl() || 'https://reg.pryv.me/service/info';
  Pryv.Browser.setupAuth(authSettings, serviceInfoUrl);
};

async function submitForm() {
  const systolic = document.getElementById('systolic').value;
  const diastolic = document.getElementById('diastolic').value;
   // We include creation of the streams anyways
   // If they already exist in the account, this will not affect the next 
   // calls while insuring their existence.
  const apiCall = [
    {
      method: 'streams.create',
      params: {
        id: 'heart',
        name: 'Heart',
        parentId: 'body'
      }
    },
    {
      method: 'streams.create',
      params: {
        id: 'blood-pressure',
        name: 'Blood pressure',
        parentId: 'heart'
      }
    },
    {
      method: 'streams.create',
      params: {
        id: 'analysis',
        name: 'Analysis',
        parentId: 'heart'
      }
    },
    {
      method: 'streams.create',
      params: {
        id: 'diagnosis',
        name: 'Diagnosis',
        parentId: 'heart'
      }
    }
  ];

  function logResultToConsole(result) {
    console.log('result: ', JSON.stringify(result));
  }

  if (!isNaN(systolic) || !isNaN(diastolic)) {
    apiCall.push(
      {
        method: 'events.create', // create the event in the corresponding stream 'blood-pressure'
        params: {
          streamId: 'blood-pressure',
          type: 'blood-pressure/mmhg-bpm', // See: https://api.pryv.com/event-types/#blood-pressure
          content: {
            systolic: Number(systolic),
            diastolic: Number(diastolic),
          }
        },
        handleResult: logResultToConsole // Pryv's lib-js per-call handler
      },
      {
        method: 'events.create', // create the event in the corresponding stream 'Analysis'
        params: {
          streamId: 'analysis',
          type: 'note/txt', // See: https://api.pryv.com/event-types/#note
          content: "Analysis of the blood pressure category based on the last entry: pending"
        },
        handleResult: logResultToConsole // Pryv's lib-js per-call handler
      }
    );
  } else {
    alert('Please enter a number for the systolic / diastolic values.');
  }

  const result = await connection.api(apiCall);
  console.log(result);
  alert('Thank you for answering these questions!');
  return false;
}