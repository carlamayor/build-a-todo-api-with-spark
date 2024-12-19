package com.teamtreehouse.techdegrees.dao;

import com.teamtreehouse.techdegrees.model.Todo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static org.junit.Assert.*;

public class Sql2oTodoDaoTest {

    private Sql2oTodoDao dao;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";

        Sql2o sql2o = new Sql2o(connectionString,  "", "");
        dao = new Sql2oTodoDao(sql2o);
        connection = sql2o.open();
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void addingTodosSetsId() throws Exception {
        Todo todo = newTestTodo();
        int originalTodoId = todo.getId();

        dao.createTodo(todo);

        assertNotEquals(originalTodoId, todo.getId());
    }

    @Test
    public void addedTodosAreReturnedFromFindAll() throws  Exception{
        Todo todo = newTestTodo();

        dao.createTodo(todo);

        assertEquals(1, dao.findAll().size());
    }

    @Test
    public void noTodosReturnsEmptyList() throws Exception {
        assertEquals(0, dao.findAll().size());
    }

    @Test
    public void existingTodosCanBeFoundById() throws Exception{
        Todo todo = newTestTodo();

        dao.createTodo(todo);

        Todo foundTodo= dao.findById(todo.getId());

        assertEquals(todo, foundTodo);
    }

    @Test
    public void todoIsUpdatedById() throws Exception{
        //Arrange - create new object
        Todo todo = newTestTodo();
        dao.createTodo(todo);

        //Retrieve original values
        int originalId = todo.getId();
        String originalName = todo.getName();
        boolean originalIsCompleted = todo.isCompleted();

        //Update name & is completed.
        todo.setName("New Test todo");
        todo.setCompleted(false);

        //Act - updates using method
        dao.updateTodo(todo);

        //Assert - fetch updated todo to verify updates.
        Todo updatedTodo = dao.findById(originalId);
        int sameId = todo.getId();
        String newName = todo.getName();
        boolean newIsCompleted = todo.isCompleted();

        //Check if everything was updated
        assertNotNull("The updated todo shouldn't be null",updatedTodo);
        assertNotEquals(originalName, newName);
        assertEquals(originalId, sameId);
        assertNotEquals(originalIsCompleted, newIsCompleted);

    }

    @Test
    public void todoIsDeletedById() throws Exception{
        Todo todo = newTestTodo();
        dao.createTodo(todo);
        dao.deleteTodo(todo.getId());

        assertEquals(0, dao.findAll().size());
    }


    private static Todo newTestTodo() {
        return new Todo("Test todo", true);
    }
}