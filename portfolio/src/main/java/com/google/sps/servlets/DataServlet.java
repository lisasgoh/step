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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {
  Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int userInput;
    try {
       userInput = getUserInput(request);
    }
    catch(IllegalArgumentException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }
    Query query = new Query("Comment");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    List<Entity> entities = results.asList(FetchOptions.Builder.withLimit(userInput));
    List<Comment> commentEntities = new ArrayList<>();
    for (Entity entity : entities) {
      long id = entity.getKey().getId();
      String comment = (String) entity.getProperty("comment");
      Comment commentEntity = new Comment(id, comment);
      commentEntities.add(commentEntity);
    }
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(commentEntities));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
        long id = Long.parseLong(request.getParameter("id"));
        Key commentEntityKey = KeyFactory.createKey("Comment", id);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.delete(commentEntityKey);
     //when "id" parameter is null
    } catch (NumberFormatException e) { 
        String comment = request.getParameter("comment");
        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("comment", comment);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
    }
    response.sendRedirect("/index.html");

  }

  private int getUserInput(HttpServletRequest request) throws IllegalArgumentException {
    // Get the input from the form.
    String userInputString = request.getParameter("value");
    // Convert the input to an int.
    int userInput = Integer.parseInt(userInputString);
    // Check that the input is in range
    if (userInput < 0 || userInput > 10) {
      throw new IllegalArgumentException("Value should be between 0 and 1.");
    }
    return userInput;
  }
}
