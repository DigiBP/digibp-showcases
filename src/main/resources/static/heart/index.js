/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

async function authorized() {
  document.getElementById('please-login').style.visibility = 'hidden';
  window.location.href = "onboarding/"
}

async function showLoginMessage() {
  document.getElementById('please-login').style.visibility = 'visible';
}

window.onload = (event) => {
  // following the APP GUIDELINES: https://api.pryv.com/guides/app-guidelines/
  const serviceInfoUrl = Pryv.Browser.serviceInfoFromUrl() || 'https://reg.pryv.me/service/info';
  Pryv.Browser.setupAuth(authSettings, serviceInfoUrl);
};