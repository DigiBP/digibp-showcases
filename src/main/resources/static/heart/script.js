var connection = null;
var username = null;

var authSettings = {
  spanButtonID: 'pryv-button', // span id the DOM that will be replaced by the Service specific button
  onStateChange: pryvAuthStateChange, // event Listener for Authentication steps
  authRequest: { // See: https://api.pryv.com/reference/#auth-request
    requestingAppId: 'heart-service', // to customize for your own app
    requestedPermissions: [
      {
        streamId: 'body',
        defaultName: 'Body',
        level: 'manage' // permissions for the app to manage data in stream 'Body'
      },
      {
        streamId: 'heart-service',
        defaultName: 'Heart-Service',
        level: 'manage' // permissions for the app to manage data in stream 'Heart-Service'
      },
      {
        streamId: 'contact',
        defaultName: 'Contact',
        level: 'manage' // permissions for the app to manage data in stream 'Contact'
      }
    ],
    clientData: {
      'app-web-auth:description': {
        'type': 'note/txt',
        'content': 'This app provides a Pryv-Camunda integration use case.' // to customize according to your own use case
      }
    },
  }
};

function pryvAuthStateChange(state) { // called each time the authentication state changes
  console.log('##pryvAuthStateChange', state);
  if (state.id === Pryv.Browser.AuthStates.AUTHORIZED) {
    connection = new Pryv.Connection(state.apiEndpoint);
    username = state.username;
    authorized();
  }
  if (state.id === Pryv.Browser.AuthStates.NEED_SIGNIN) {
    connection = null;
    showLoginMessage();
  }
}