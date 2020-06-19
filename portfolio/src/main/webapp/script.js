// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

document.addEventListener('DOMContentLoaded',() => {    
    textAnimation(event);
    fetchLoginStatus();
});

function textAnimation(event) {
    var dataText = [ "Programmer", "Learner" , "Dancer"];
  // type one text in the typwriter, keeps calling itself until the text is finished
    function typeWriter(text, i, fnCallback) {
    // check if text isn't finished yet
      if (i < (text.length)) {
        // add next character
        document.querySelector("#typewriter").innerHTML = text.substring(0, i+1) +'<span aria-hidden="true"></span>';
        // wait for a while and call this function again for next character
        setTimeout(function() {
          typeWriter(text, i + 1, fnCallback)
        }, 100);
      }
    // text finished, call callback if there is a callback function
      else if (typeof fnCallback == 'function') {
      // call callback after timeout
        setTimeout(fnCallback, 700);
      }
    }
  // start a typewriter animation for a text in the dataText array
    function StartTextAnimation(i) {
      if (i == dataText.length) {
        i = 0;
      }
    // start typewriter animation
      typeWriter(dataText[i], 0, function(){
       // after callback (and whole text has been animated), start next text
        StartTextAnimation(i + 1);
       });
    }
  // start the text animation
    StartTextAnimation(0);
}

function getComments() {
    var value = document.getElementById("number-comments").value;
    fetch("/comments?value="+value).then(response => response.json()).then((comments) => {
        // Build the list of comment entries.
      const commentListElement = document.getElementById('comment-list');
      commentListElement.innerHTML = "";
      comments.forEach((comment) => {
      commentListElement.appendChild(createCommentElement(comment));
      });
    });
}

/** Creates an <li> element containing text. */
function createCommentElement(commentEntity) {
  const commentElement = document.createElement('div');
  commentElement.className = 'comment border border-info';

  const textElement = document.createElement('span');
  textElement.innerText = commentEntity.userEmail + ": " + commentEntity.comment;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.className= 'btn btn-danger float-right';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(commentEntity);
    commentElement.remove();
  });

  commentElement.appendChild(textElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

function deleteComment(commentEntity) {
    const params = new URLSearchParams();
    params.append('id', commentEntity.id);
    fetch('/comments', {method: 'POST', body: params});
}

function fetchLoginStatus() {
    fetch("/login").then(response => response.json()).then((user) => {
      const url  = document.getElementById('login-logout-url');
      if (user.loginStatus) {
        const commentForm = document.getElementById("comment-form");
        commentForm.style.display = "block";
        url.innerHTML = "<p>Logout <a href=\"" + user.url + "\">here</a>.</p>";
      }
      else {
        url.innerHTML = "<p>Login <a href=\"" + user.url + "\">here</a> to submit comments.</p>";
      });
}
