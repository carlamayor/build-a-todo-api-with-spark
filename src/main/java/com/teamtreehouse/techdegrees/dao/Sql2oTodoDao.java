package com.teamtreehouse.techdegrees.dao;

import com.teamtreehouse.techdegrees.exc.DaoException;
import com.teamtreehouse.techdegrees.model.Todo;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oTodoDao implements TodoDao {

    private final Sql2o sql2o;

    public Sql2oTodoDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    @Override
    public void createTodo(Todo todo) throws DaoException {
        String sql = "INSERT INTO todos(name, is_completed) VALUES (:name, :is_completed)";
        try (Connection con = sql2o.open()) {
            int id = (int) con.createQuery(sql)
                    .addParameter("name", todo.getName())
                    .addParameter("is_completed", todo.isCompleted())
                    .executeUpdate()
                    .getKey();
            todo.setId(id);
        } catch (Sql2oException ex) {
            throw new DaoException(ex, "Problem adding Todo");
        }
    }


    @Override
    public void updateTodo(Todo todo) {
        String sql = "UPDATE todos SET name = :name, is_completed = :is_completed WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("name", todo.getName())
                    .addParameter("is_completed", todo.isCompleted())
                    .addParameter("id", todo.getId())
                    .executeUpdate();

        }
    }

    @Override
    public void deleteTodo(int id) {
        String sql = "DELETE FROM todos WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }

    @Override
    public List<Todo> findAll() {
        try(Connection con = sql2o.open()) {
            return con.createQuery("SELECT id, name, is_completed AS isCompleted FROM todos")
                    .executeAndFetch(Todo.class);
        }
    }

    @Override
    public Todo findById(int id) {
        try(Connection con = sql2o.open()){
            return con.createQuery("SELECT id, name, is_completed AS isCompleted  FROM todos WHERE id = :id")
                    .addParameter("id", id)
                    .executeAndFetchFirst(Todo.class);

        }
    }


}
