package com.teamtreehouse.techdegrees;

import com.google.gson.Gson;
import com.teamtreehouse.techdegrees.dao.Sql2oTodoDao;
import com.teamtreehouse.techdegrees.model.Todo;
import com.teamtreehouse.testing.AppClient;
import com.teamtreehouse.testing.AppResponse;
import org.junit.*;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import spark.Spark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AppTest {


    public static final String PORT = "4568";
    public static final String TEST_DATASOURCE = "jdbc:h2:mem:testing";
    private Connection conn;
    private AppClient client;
    private Gson gson;
    private Sql2oTodoDao todoDao;


    @BeforeClass
    public static void startServer() {
        String[] args = {PORT, TEST_DATASOURCE};
        App.main(args);
    }

    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }

    @Before
    public void setUp() throws Exception{
        Sql2o sql2o = new Sql2o(TEST_DATASOURCE + ";INIT=RUNSCRIPT from 'classpath:db/init.sql'", "", "");
        todoDao = new Sql2oTodoDao(sql2o);
        conn = sql2o.open();
        client = new AppClient("http://localhost:"+ PORT);
        gson = new Gson();
    }

    @After
    public void tearDown() throws Exception{
        conn.close();
    }

    @Test
    public void multipleToDosCreatedAreFetchedWithFindAll() throws Exception{
        Todo todo = new Todo("Todo 1", true);
        Todo todo2 = new Todo("Todo 2", false);
        Todo todo3 = new Todo("Todo 3", true);
        todoDao.createTodo(todo);
        todoDao.createTodo(todo2);
        todoDao.createTodo(todo3);

        AppResponse res = client.request("GET", "/api/v1/todos");
        Todo[] todos = gson.fromJson(res.getBody(), Todo[].class);


        assertEquals(200, res.getStatus());
        assertEquals(3, todos.length);

    }

    @Test
    public void creatingTodoIsSuccessfullyPosted() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Testing todo action");
        values.put("isCompleted", false);

        AppResponse res = client.request("POST", "/api/v1/todos", gson.toJson(values));

        List<Todo> todos = todoDao.findAll();
        Todo createdTodo = todos.stream()
                .filter(todo -> todo.getName().equals("Testing todo action"))
                .findFirst()
                .orElse(null);
        assertNotNull(createdTodo);
        assertEquals("Testing todo action", createdTodo.getName());
        assertFalse(createdTodo.isCompleted());
    }

    @Test
    public void creatingTodoReturnsCreatedStatus() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Testing todo action");
        values.put("isCompleted", false);

        AppResponse res = client.request("POST", "/api/v1/todos", gson.toJson(values));

        assertEquals(201, res.getStatus());
    }


    @Test
    public void todosCanBeAccessedById() throws Exception {
        Todo todo = newTestTodo();
        todoDao.createTodo(todo);

        AppResponse res = client.request("GET", "/api/v1/todos/" + todo.getId());
        Todo fetchedTodo = gson.fromJson(res.getBody(), Todo.class);

        assertEquals(todo, fetchedTodo);
    }

    @Test
    public void missingTodoReturnsNotFoundStatus() throws Exception {
        AppResponse res = client.request("GET", "/api/v1/todos/42");

        assertEquals(404, res.getStatus());
    }

    @Test
    public void todoSuccessfullyUpdatedById() throws Exception {
        Todo originalTodo = newTestTodo();
        todoDao.createTodo(originalTodo);
        Map<String, Object> values = new HashMap<>();
        values.put("name", "New updated todo");
        values.put("isCompleted", false);

        AppResponse res = client.request("PUT", "/api/v1/todos/" + originalTodo.getId(), gson.toJson(values));
        Todo updatedTodo = gson.fromJson(res.getBody(), Todo.class);

        assertEquals("New updated todo", updatedTodo.getName());
        assertFalse(updatedTodo.isCompleted());
        assertEquals(originalTodo.getId(), updatedTodo.getId());
    }

    @Test
    public void todoUpdateShowsSuccessfulStatus() throws Exception{
        Todo originalTodo = newTestTodo();
        todoDao.createTodo(originalTodo);
        Map<String, Object> values = new HashMap<>();
        values.put("name", "New updated todo");
        values.put("isCompleted", false);

        AppResponse res = client.request("PUT", "/api/v1/todos/" + originalTodo.getId(), gson.toJson(values));

        assertEquals(200, res.getStatus());
    }

    @Test
    public void deletingTodoShowsSuccessfulStatus() throws Exception{
        Todo todo = newTestTodo();
        todoDao.createTodo(todo);
        int id = todo.getId();

        AppResponse res = client.request("DELETE", "/api/v1/todos/" + id);

        assertEquals(204, res.getStatus());
    }

    @Test
    public void deletingTodoByIdIsSuccessful() throws Exception{
        Todo todo = newTestTodo();
        todoDao.createTodo(todo);
        int id = todo.getId();

        AppResponse res = client.request("DELETE", "/api/v1/todos/" + id);
        assertNull(todoDao.findById(id));
    }



    private static Todo newTestTodo() {
        return new Todo("Test todo", true);
    }
}