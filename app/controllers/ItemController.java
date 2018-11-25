package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.*;
import org.mongodb.morphia.Key;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.*;
import play.libs.Json;
import utils.DateTime;
import utils.Response;

import java.util.ArrayList;
import java.util.List;

public class ItemController  extends Controller {

    @Inject
    private IBookRepository bookRepo;

    @Inject
    private IDVDRepository dvdRepo;

    @Inject
    private IReaderRepository readerRepo;

    @Inject
    private IAuthorRepository authorRepo;

    @Inject
    private IActorRepository actorRepo;

    private HttpExecutionContext httpExecutionContext;

    @Inject
    public ItemController(HttpExecutionContext ec) {
        this.httpExecutionContext = ec;
    }

    public Result getAll() {
        List<Item> itemList = new ArrayList<>();
        List<DVD> dvds = dvdRepo.findAll();
        List<Book> books = bookRepo.findAll();
        itemList.addAll(dvds);
        itemList.addAll(books);
        return ok(Json.toJson(itemList));
    }


    public Result saveDvd() {
        JsonNode json = request().body().asJson();
        if(json == null){
            return badRequest(Response.generateResponse("Expecting Json data", false));
        }

        DVD deserializedDVD = Json.fromJson(json, DVD.class);

        // get reader by id
        Reader reader = readerRepo.findById(deserializedDVD.getCurrentReader().getId());

        List<Actor> actors = deserializedDVD.getActors();

        for (int i=0; i< actors.size(); i++) {
            actorRepo.save(actors.get(i));
        }

        deserializedDVD.setCurrentReader(reader);

        dvdRepo.save(deserializedDVD);
        return ok("insert dvdRepo success");
    }

    public Result saveBook() {
        JsonNode json = request().body().asJson();
        if(json == null){
            return badRequest(Response.generateResponse("Expecting Json data", false));
        }

        Book deserializedBook = Json.fromJson(json, Book.class);

        // get reader by id
        Reader reader = readerRepo.findById(deserializedBook.getCurrentReader().getId());

        List<Author> authors = deserializedBook.getAuthor();

        for (int i=0; i< authors.size(); i++) {
            authorRepo.save(authors.get(i));
        }
        deserializedBook.setCurrentReader(reader);

        Key<Book> returned = bookRepo.save(deserializedBook);
        return ok(String.valueOf(returned.getId()));
    }

    public Result borrowBook(String id, String date, String borrower) {
        if(id == null || date == null || borrower == null){
            return badRequest(Response.generateResponse("Expecting required data", false));
        }

        String[] dateArr = date.split("-");

        // find id by object id of the book
        Book book = bookRepo.findById(id);

        book.setBorrowedDate(new DateTime(Integer.parseInt(dateArr[0]),
                Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2])));

        DateTime dateTime = new DateTime(Integer.parseInt(dateArr[0]),
                Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]));
        book.getCurrentReader().setName(borrower);

        // later take date from request or get it by reader id
        Reader reader = book.getCurrentReader();
        reader.setName(borrower);

        boolean isSth = bookRepo.updateBorrowing(id, book, dateTime, reader);
        System.out.println(isSth);
        return ok(Json.toJson(book));
    }

    public Result borrowDvd(String id, String date, String borrower) {
        if(id == null || date == null || borrower == null){
            return badRequest(Response.generateResponse("Expecting required data", false));
        }

        String[] dateArr = date.split("-");

        // find id by object id of the book
        DVD dvd = dvdRepo.findById(id);

        dvd.setBorrowedDate(new DateTime(Integer.parseInt(dateArr[0]),
                Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2])));

        DateTime dateTime = new DateTime(Integer.parseInt(dateArr[0]),
                Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]));
        dvd.getCurrentReader().setName(borrower);

        // later take date from request or get it by reader id
        Reader reader = dvd.getCurrentReader();
        reader.setName(borrower);

        boolean isSth = dvdRepo.updateBorrowing(id, dvd, dateTime, reader);
        System.out.println(isSth);
        return ok(Json.toJson(dvd));
    }

    public Result returnBook(String id) {
        if(id == null){
            return badRequest(Response.generateResponse("Expecting required data", false));
        }

        boolean isSth = bookRepo.updateReturning(id);
        System.out.println(isSth);
        return ok(Json.toJson("Success returning the book"));
    }

    public Result deleteBook(String id) {
        if(id == null){
            return badRequest(Response.generateResponse("Expecting required data", false));
        }

        boolean result = bookRepo.delete(id);
        if (result) {
            return ok(Json.toJson("Success"));
        } else {
            return badRequest(Response.generateResponse("Invalid data provided", false));
        }
    }
}
