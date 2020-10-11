/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

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
  const name = document.getElementById('name').value;
  const email = document.getElementById('email').value;
  const mobile = document.getElementById('mobile').value;
  // We include creation of the streams anyways
  // If they already exist in the account, this will not affect the next 
  // calls while insuring their existence.
  const apiCall = [
    {
      method: 'streams.create',
      params: {
        id: 'contact-email',
        name: 'Contact-Email',
        parentId: 'contact'
      }
    },
    {
      method: 'streams.create',
      params: {
        id: 'contact-name',
        name: 'Contact-Name',
        parentId: 'contact'
      }
    },
    {
      method: 'streams.create',
      params: {
        id: 'contact-mobile',
        name: 'Contact-Mobile',
        parentId: 'contact'
      }
    }
  ];

  function logResultToConsole(result) {
    console.log('result: ', JSON.stringify(result));
  }

  if (name && email && mobile) {
    apiCall.push(
      {
        method: 'events.create', // create the event in the corresponding stream 'Contact-Email'
        params: {
          streamId: 'contact-email',
          type: 'note/txt', // See: https://api.pryv.com/event-types/#note
          content: email
        },
        handleResult: logResultToConsole // Pryv's lib-js per-call handler
      },
      {
        method: 'events.create', // create the event in the corresponding stream 'Contact-Name'
        params: {
          streamId: 'contact-name',
          type: 'call/name', // See: https://api.pryv.com/event-types/#call
          content: name
        },
        handleResult: logResultToConsole // Pryv's lib-js per-call handler
      },
      {
        method: 'events.create', // create the event in the corresponding stream 'Contact-Mobile'
        params: {
          streamId: 'contact-mobile',
          type: 'call/telephone', // See: https://api.pryv.com/event-types/#call
          content: mobile
        },
        handleResult: logResultToConsole // Pryv's lib-js per-call handler
      }
    );
  } else {
    alert('Please enter valid data.');
  }

  const result = await connection.api(apiCall);
  console.log(result);

  const heartEndpoint = window.origin + '/api/heart/v1/onboarding';
  const xhr = new XMLHttpRequest();
  const json = {
    "pryvTokenEndpoint": Pryv.utils.buildPryvApiEndpoint({
      token: connection.token,
      endpoint: connection.endpoint})
  };
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      window.location.href = "../recording/"
    }
  }
  xhr.open('POST', heartEndpoint);
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.send(JSON.stringify(json));

  return false;
}