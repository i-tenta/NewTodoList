package com.example.todolist.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {
	//JPQLによる検索
	List<Todo>findByJPQL(TodoQuery todoQuery);
	
	//CriteriaAPIによる検索
	Page<Todo>findByCriteria(TodoQuery todoQuery,Pageable pageable);
}
