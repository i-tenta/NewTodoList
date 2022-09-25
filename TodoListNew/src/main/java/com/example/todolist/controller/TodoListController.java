package com.example.todolist.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.servlet.ModelAndView;

import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;
	
	@PostConstruct
	public void init() {
		todoDaoImpl=new TodoDaoImpl(entityManager);
	}
	
	//ToDo一覧表示のページにとんだとき
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv,
			@PageableDefault(page=0,size=5,sort="id")
			Pageable pageable) {
		mv.setViewName("todoList");
		Page<Todo> todoPage=todoRepository.findAll(pageable);
		mv.addObject("todoPage",todoPage);
		mv.addObject("todoQuery", new TodoQuery());
		
		mv.addObject("todoList",todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());
		return mv;
	}
	
	//ToDo入力フォーム表示
	//新規追加のリンクをクリックされたとき
	@PostMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv) {
		
		mv.setViewName("todoForm");  //入力画面を表示
		mv.addObject("todoData", new TodoData());   //画面に渡し初期状態
		session.setAttribute("mode", "create");
		return mv;
	}
	
	@GetMapping("/todo/{id}")
	public ModelAndView todoById(
			@PathVariable(name="id")
			int id,
			ModelAndView mv) {
		mv.setViewName("todoForm");
		Todo todo=todoRepository.findById(id).get();
		mv.addObject("todoData", todo);
		session.setAttribute("mode", "update");
		return mv;
	}
	
	
	//登録ボタンが押されたとき
	@PostMapping("/todo/create/do")
	public ModelAndView createTodo(
			@ModelAttribute
			@Validated  //エラーチェック
			TodoData todoData,
			BindingResult result,  //todoDataのチェック結果が入る
			ModelAndView mv
			) {
		//エラーチェック
		boolean isValid=todoService.isValid(todoData,result);
		
		if(!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo=todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return showTodoList(mv, null);
		}else {
			//エラーあり
			mv.setViewName("todoForm");
			mv.addObject("todoData",todoData);
			return mv;
		}
	}
	
	//ＴｏＤｏ一覧画面に戻る
	//キャンセルボタンがクリックされたとき
	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";
	}
	
	//更新ボタンがクリックされたとき
	@PostMapping("/todo/update")
	public String updateTodo(
			@ModelAttribute
			@Validated
			TodoData todoData,
			BindingResult result,
			Model model) {
		//エラーチェック
		boolean isValid=todoService.isValid(todoData, result);
		if(!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo=todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		}else {
			//エラーあり
			return "todoForm";
		}
	}
	
	//削除ボタンがクリックされたとき
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}
	
	//検索
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(
			@ModelAttribute
			TodoQuery todoQuery,
			BindingResult result,
			@PageableDefault(page=0,size=5)
			Pageable pageable,
			ModelAndView mv
			) {
		mv.setViewName("todoList");
		
		Page<Todo> todoPage=null;
		if(todoService.isValid(todoQuery, result)) {
			//エラーがなければ検索
			todoPage=todoDaoImpl.findByCriteria(todoQuery,pageable);
			//入力された検索条件をsessionに保存
			mv.addObject("todoPage",todoPage);
			mv.addObject("todoList",todoPage.getContent());
		}else {
			//エラーがあった場合検索
			mv.addObject("todoPage", null);
			mv.addObject("todoList", null);
		}
		
		return mv;
	}
	
	@GetMapping("/todo/query")
	public ModelAndView queryTodo(
			@PageableDefault(page=0,size=5)
			Pageable pageable,
			ModelAndView mv
			) {
		mv.setViewName("todoList");
		
		//sessionに保存されている条件で検索
		TodoQuery todoQuery=(TodoQuery)session.getAttribute("todoQuery");
		Page<Todo>todoPage=todoDaoImpl.findByCriteria(todoQuery, pageable);
		
		mv.addObject("todoQuery", todoQuery);
		mv.addObject("todoPage", todoPage);
		mv.addObject("todoList", todoPage.getContent());
		
		return mv;
	}
	
	


}
