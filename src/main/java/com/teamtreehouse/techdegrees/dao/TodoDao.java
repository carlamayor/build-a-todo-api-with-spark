package com.teamtreehouse.techdegrees.dao;

import com.teamtreehouse.techdegrees.exc.DaoException;
import com.teamtreehouse.techdegrees.model.Todo;

import java.util.List;


public interface TodoDao {

    //create, update, delete, and findAll methods
    void createTodo(Todo todo) throws DaoException;
    void updateTodo(Todo todo) ;
    void deleteTodo(int id) ;
    List<Todo> findAll();
    Todo findById(int id);

}
