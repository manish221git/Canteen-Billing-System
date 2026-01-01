package com.canteen.dao;

import com.canteen.exception.DatabaseException;
import java.util.List;

/**
 * Generic DAO interface for CRUD operations
 * Demonstrates DAO pattern and Generics
 */
public interface GenericDAO<T> {
    /**
     * Insert a new entity
     */
    int insert(T entity) throws DatabaseException;
    
    /**
     * Update an existing entity
     */
    boolean update(T entity) throws DatabaseException;
    
    /**
     * Delete an entity by ID
     */
    boolean delete(int id) throws DatabaseException;
    
    /**
     * Find an entity by ID
     */
    T findById(int id) throws DatabaseException;
    
    /**
     * Get all entities
     */
    List<T> findAll() throws DatabaseException;
}
