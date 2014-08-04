package com.sample.todolist;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class ToDoListApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(ToDoList.class);
		return classes;
	}

}
