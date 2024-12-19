package com.teamtreehouse.techdegrees;


import com.google.gson.Gson;
import com.teamtreehouse.techdegrees.dao.Sql2oTodoDao;
import com.teamtreehouse.techdegrees.dao.TodoDao;
import com.teamtreehouse.techdegrees.exc.ApiError;
import com.teamtreehouse.techdegrees.model.Todo;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {
        staticFileLocation("/public");

        String datasource = "jdbc:h2:~/todos.db";
        if(args.length >0) {
            if (args.length != 2) {
                System.out.println("java Api <port> <datasource>");
                System.exit(0);
            }
            port(Integer.parseInt(args[0]));
            datasource = args[1];
        }
        Sql2o sql2o = new Sql2o(
                String.format("%s;INIT=RUNSCRIPT from 'classpath:db/init.sql'", datasource)
                , "", "");
        TodoDao todoDao = new Sql2oTodoDao(sql2o);//interface = implementation
        Gson gson = new Gson();

        get("/api/v1/todos", "appication/json", (req, res) ->
        todoDao.findAll(), gson::toJson);


        post("/api/v1/todos", (req, res) -> {
            Todo todo = gson.fromJson(req.body(), Todo.class);
            todoDao.createTodo(todo);
            res.status(201);
            return todo;
        }, gson::toJson);


        get("/api/v1/todos/:id", "appication/json", (req, res) -> {
                    int id = Integer.parseInt(req.params("id"));
                    Todo foundTodo = todoDao.findById(id);
                    if(foundTodo == null) {
                        throw new ApiError(404, "Could not find todo");
                    }
                    return foundTodo;
                }, gson::toJson);




        put("/api/v1/todos/:id", "application/json" ,(req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Todo originalTodo = todoDao.findById(id);
            if (originalTodo == null){
                res.status(404);
                return gson.toJson("Todo not found");
            }
            Todo newToDo = gson.fromJson(req.body(), Todo.class);

            originalTodo.setName(newToDo.getName());
            originalTodo.setCompleted(newToDo.isCompleted());
            todoDao.updateTodo(originalTodo);
            res.status(200);
            return originalTodo;
        }, gson::toJson);

        delete("/api/v1/todos/:id","application/json" , (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            todoDao.deleteTodo(id);
            res.status(204);
            return "";
        });

        exception(ApiError.class, (exc, req, res) -> {//when an exception happens we give back exception, request and response
            ApiError err = (ApiError) exc;
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("status", err.getStatus());
            jsonMap.put("errorMessage", err.getMessage());
            res.type("application/json");
            res.status(err.getStatus());
            res.body(gson.toJson(jsonMap));
        });

        after((req, res) -> {
            res.type("application/json");//letting user know the response is application/json in all cases
        });



    }

}
