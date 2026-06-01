// ExampleBookServlet.java
// This servlet provides a RESTful API for managing a collection of books in memory.
// It supports CRUD operations: create, read, update, and delete books via HTTP methods.
// All responses are in JSON format. This is for demonstration and does not persist data.
//
// Author: [Your Name]
// Date: [Date]
//

package com.example.jobrec.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.jobrec.entity.ExampleBook;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ExampleBookServlet provides RESTful endpoints for managing books.
 * Supported operations:
 * - GET:    List all books or search by keyword/category
 * - POST:   Add a new book
 * - PUT:    Update an existing book by ISBN
 * - DELETE: Remove a book by ISBN
 */
@WebServlet(name = "ExampleBookServlet", urlPatterns = {"/example_book"})
public class ExampleBookServlet extends HttpServlet {
    // In-memory thread-safe book list
    private static final List<ExampleBook> books = new CopyOnWriteArrayList<>();

    static {
        // Add a sample book for demonstration
        books.add(new ExampleBook("Harry Potter and the Sorcerer's Stone", "JK Rowling",
                "October 1, 1998", 11.99, "USD", 309, "Harry Potter", "en_US", "0590353403"));
    }

    /**
     * Handles GET requests to list all books or search by keyword/category.
     * Query params: keyword, category
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        List<ExampleBook> result = books;
        if (keyword != null && !keyword.isEmpty()) {
            result = books.stream().filter(b -> b.title.toLowerCase().contains(keyword.toLowerCase()) ||
                    b.author.toLowerCase().contains(keyword.toLowerCase())).toList();
        } else if (category != null && !category.isEmpty()) {
            result = books.stream().filter(b -> b.series != null && b.series.toLowerCase().contains(category.toLowerCase())).toList();
        }
        mapper.writeValue(response.getWriter(), result);
    }

    /**
     * Handles POST requests to add a new book.
     * Expects JSON body matching ExampleBook fields.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ExampleBook newBook = mapper.readValue(request.getReader(), ExampleBook.class);
        // Check for duplicate ISBN
        boolean exists = books.stream().anyMatch(b -> b.isbn.equals(newBook.isbn));
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        if (exists) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Book with this ISBN already exists.");
        } else {
            books.add(newBook);
            jsonResponse.put("status", "ok");
            jsonResponse.put("message", "Book added successfully.");
        }
        response.getWriter().print(jsonResponse);
    }

    /**
     * Handles PUT requests to update an existing book by ISBN.
     * Expects JSON body with updated fields and 'isbn' to identify the book.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ExampleBook updatedBook = mapper.readValue(request.getReader(), ExampleBook.class);
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        Optional<ExampleBook> existing = books.stream().filter(b -> b.isbn.equals(updatedBook.isbn)).findFirst();
        if (existing.isPresent()) {
            books.remove(existing.get());
            books.add(updatedBook);
            jsonResponse.put("status", "ok");
            jsonResponse.put("message", "Book updated successfully.");
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Book not found.");
        }
        response.getWriter().print(jsonResponse);
    }

    /**
     * Handles DELETE requests to remove a book by ISBN.
     * Expects query param 'isbn'.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String isbn = request.getParameter("isbn");
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        if (isbn == null || isbn.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "ISBN parameter is required.");
        } else {
            boolean removed = books.removeIf(b -> b.isbn.equals(isbn));
            if (removed) {
                jsonResponse.put("status", "ok");
                jsonResponse.put("message", "Book deleted successfully.");
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Book not found.");
            }
        }
        response.getWriter().print(jsonResponse);
    }
}

