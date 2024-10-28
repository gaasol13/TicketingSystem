package com.poortoys.examples.dao;
import java.util.List;


//DEfine CRUD operations to ensure consistency across different DAO
public interface InterfaceDAO<T, ID> {
	void create(T entity);
    T read(ID id);
    void update(T entity);
    void delete(T entity);
    List<T> findAll();

}
