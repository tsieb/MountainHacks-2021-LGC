package com.bookOfTmrwServer.demo.controller;


import com.bookOfTmrwServer.demo.model.Book;
import com.bookOfTmrwServer.demo.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
public class controller {
    private ArrayList<User> userArrayList;

    @PostConstruct
    public void initIt(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            userArrayList = new ArrayList<>(Arrays.asList(
                    mapper.readValue(
                            Paths.get("src/main/resources/data/userBookList.json").toFile(),
                            User[].class)));
//            System.out.println("Book: "+ userArrayList.get(0).getBook(0).getName());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("/api/userList/getBook/{id}")
    public ArrayList<Book> getBookList (@PathVariable String id, HttpServletResponse response) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < userArrayList.size(); i++){
            if (userArrayList.get(i).getId().equals(id)){
                System.out.println("getBookList called, returning userArrayList.get(id)");
                response.setStatus(200);
                return userArrayList.get(i).getBookArrayList();
            }
        }
        ArrayList<Book> bookArrayList = new ArrayList<>();
        User user = new User(id, bookArrayList);
        userArrayList.add(user);
        mapper.writeValue(Paths.get("src/main/resources/data/userBookList.json").toFile(), userArrayList);

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getOutputStream().print(response.getStatus());
        response.flushBuffer();
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "ID not found. Created.");
        return user.getBookArrayList();
    }

    @PostMapping("/api/userList/addBook/{id}")
    public void addBookToUser(@PathVariable String id, @RequestBody Book book, HttpServletResponse response){
        try{
            Boolean foundID = false;
            ObjectMapper mapper = new ObjectMapper();

            for (int i =0 ; i < userArrayList.size(); i++){
                if (userArrayList.get(i).getId().equals(id)){
                    foundID = true;
                    userArrayList.get(i).getBookArrayList().add(book);
                }
            }
            //add new user
            if (!foundID){
                System.out.println("Found new user!");
                ArrayList<Book> bookArrayList = new ArrayList<>();
                bookArrayList.add(book);
                User user = new User(id, bookArrayList);
                userArrayList.add(user);
            }
            mapper.writeValue(Paths.get("src/main/resources/data/userBookList.json").toFile(), userArrayList);
            response.setStatus(201);
            System.out.println("created book " + book.getName());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping("/api/userList/delBook/{id}")
    public void deleteBookFromUser(@PathVariable String id, @RequestBody Book book, HttpServletResponse response){
        try{

            ObjectMapper mapper = new ObjectMapper();

            for (int i =0 ; i < userArrayList.size(); i++){
                if (userArrayList.get(i).getId().equals(id)){
                    Boolean isBookDeleted = userArrayList.get(i).delBookFromList(book);
                    if (isBookDeleted) {
                        response.setStatus(201);
                        mapper.writeValue(Paths.get("src/main/resources/data/userBookList.json").toFile(), userArrayList);
                        return;
                    }
                    else {
                        break;
                    }
                }
            }

            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getOutputStream().print("Can't find ID");
            response.flushBuffer();
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Id invalid");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
