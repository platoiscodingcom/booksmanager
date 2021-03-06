package com.example.booksmanager.controller;

import com.example.booksmanager.domain.Author;
import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.service.AuthorService;
import com.example.booksmanager.service.BookService;
import com.example.booksmanager.service.CategoryService;
import com.example.booksmanager.support.Message;
import com.example.booksmanager.support.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.util.Set;

/**
 * @author platoiscoding.com
 */
@Controller
public class BookController {

    //view templates
    protected static final String BOOK_VIEW = "books/showBook";                    //view template for single book
    protected static final String BOOK_ADD_FORM_VIEW = "books/newBook";            //form for new book
    protected static final String BOOK_EDIT_FORM_VIEW = "books/editBook";          //form for editing a book
    protected static final String BOOK_LIST_VIEW = "books/allBooks";               //list view of books with pagination

    //messages
    protected static final String NEW_BOOK_SUCCESS = "New Book has been added.";
    protected static final String NO_BOOKS_IN_DB_INFO = "There are no Books in the Database.";
    protected static final String BOOK_UPDATED_SUCCESS = "Book has been updated.";
    protected static final String BOOK_DELETED_SUCCESS = "Book has been deleted.";
    protected static final String FIELD_VALIDATION_ERROR = "Please correct the field errors.";
    protected static final String MUST_BE_AT_LEAST_ONE_AUTHOR_AND_CATEGRORY = "First, create at least one Category and one Author to add a new Book.";
    protected static final String BOOK_MUST_AT_LEAST_HAVE_ONE_CATEGORY = "Couldn't remove Book. A Book must have at least one Category.";
    protected static final String BOOK_ALREADY_EXISTS= "A Book of this title already exists. Please choose another title.";

    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private Message message;
    @Autowired
    private PageModel pageModel;

    /**
     * GET book by id
     * After redirect from book/create: model contains attribute "message"(success)
     * @param id        book_id
     * @param model     attributeValues
     * @return          view template for single book
     */
    @RequestMapping( path = "/book/{id}")
    public String showSingleBook(@PathVariable("id") long id, Model model) {
        if(!model.containsAttribute("message")){
            message.reset();
            model.addAttribute("message", message);
        }
        model.addAttribute("book", bookService.findById(id));
        return BOOK_VIEW;
    }

    /**
     * GET all books from database
     * @return              list view of books
     */
    @RequestMapping({"/books", "/"})
    public ModelAndView showAllBooks(Model model) {

        if(!model.containsAttribute("message")){ message.reset(); }
        if(bookService.getAll().isEmpty()) message.setInfo(NO_BOOKS_IN_DB_INFO);

        ModelAndView modelAndView = new ModelAndView(BOOK_LIST_VIEW);
        pageModel.initPageAndSize();
        modelAndView.addObject("books", bookService.findAll(PageRequest.of(pageModel.getPAGE(), pageModel.getSIZE())));
        modelAndView.addObject("message", message);

        return modelAndView;
    }

    /**
     * FORM for NEW Book
     * in case of redirection model will contain book
     * @param model     attributesValues
     * @return          BOOK_ADD_FORM_VIEW
     */
    @RequestMapping(path = "/book/new")
    public String newBookForm(Model model) {

        /* if not redirected*/
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", new Book());
        }

        Set<Category> allCategories = categoryService.getAll();
        Set<Author> allAuthors = authorService.getAll();

        if(allCategories.isEmpty() || allAuthors.isEmpty()){
            message.setInfo(MUST_BE_AT_LEAST_ONE_AUTHOR_AND_CATEGRORY);
        }
        model.addAttribute("message", message);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("allAuthors", allAuthors);
        return BOOK_ADD_FORM_VIEW;
    }

    /**
     * CREATE NEW book checks...
     *          (1)field values for errors
     *          (2)whether database already contains a book with the same name and author as field values
     * After the redirect: flash attributes pass attributes to the model
     * @param book          entity
     * @param result        result of validation of field values from BOOK_ADD_FORM_VIEW
     * @param attr          stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/book/new'
     *          else:      redirect: '/book/{bookId}'
     */
    //TODO gibt bei einer leeren categorie keinen field error aus
    @RequestMapping(path = "/book/create", method = RequestMethod.POST)
    public String createBook(@Valid Book book, BindingResult result, RedirectAttributes attr) {

        if (result.hasErrors() || !bookService.titleValid(book)) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.book", result);
            attr.addFlashAttribute("book", book);
            if(!bookService.titleValid(book)){
                message.setError(BOOK_ALREADY_EXISTS);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            return "redirect:/book/new";
        }

        Book createdBook = bookService.create(book);
        message.reset();
        message.setSuccess(NEW_BOOK_SUCCESS);
        return "redirect:/book/" + createdBook.getId();
    }

    /**
     * FORM for EDIT book
     * In case of redirect model will contain "book"
     * @param id        book_id
     * @param model     attributeValues
     * @return          BOOK_EDIT_FORM_VIEW
     */
    @GetMapping("/book/{id}/edit")
    public String editBookForm(@PathVariable("id") long id, Model model) {

        if (!model.containsAttribute("book")) {
            model.addAttribute("book", bookService.findById(id));
        }

        model.addAttribute("allCategories", categoryService.getAll());
        model.addAttribute("allAuthors", authorService.getAll());
        model.addAttribute("message", message);
        return BOOK_EDIT_FORM_VIEW;
    }

    /**
     * UPDATE book with field values from BOOK_EDIT_FORM_VIEW
     * After the redirect: flash attributes pass attributes to the model
     * @param bookId                book_id
     * @param bookDetails       entity
     * @param result            result of validation of field values from BOOK_EDIT_FORM_VIEW
     * @param attr              stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/book/{bookId}/edit'
     *          else:      redirect: '/book/{bookId}'
     */
    @RequestMapping(path = "/book/{id}/update", method = RequestMethod.POST)
    public String updateBook(@PathVariable("id") long bookId, @Valid Book bookDetails,
                             BindingResult result, RedirectAttributes attr){

        if (result.hasErrors() || !bookService.titleValid(bookDetails)) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.book", result);
            attr.addFlashAttribute("book", bookDetails);
            if(!bookService.titleValid(bookDetails)){
                message.setError(BOOK_ALREADY_EXISTS);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            return "redirect:/book/" + bookId + "/edit";
        }
        bookService.update(bookId, bookDetails);
        message.setSuccess(BOOK_UPDATED_SUCCESS);
        attr.addFlashAttribute("message", message);
        return "redirect:/book/" + bookId;
    }

    /**
     * DELETE book by id from database
     * @param bookId            book_id
     * @return              redirect: '/books'
     */
    @RequestMapping(path = "/book/{id}/delete", method = RequestMethod.GET)
    public String deleteBook(@PathVariable("id") long bookId) {
        bookService.delete(bookId);
        message.setSuccess(BOOK_DELETED_SUCCESS);
        return "redirect:/books";
    }

    /**
     * REMOVE a book from a category
     * @param bookId        book_id
     * @param catId         category_id
     * @return              redirect: '/categories'
     */
    @RequestMapping(path = "/book/{bookId}/removeFromCategory/{catId}", method = RequestMethod.GET)
    public String removeBookFromCategory(@PathVariable("bookId") long bookId, @PathVariable("catId") long catId, RedirectAttributes attr) {

        Book book = bookService.findById(bookId);
        Category category = categoryService.findById(catId);

        if(bookService.removeFromCategory(book, category)){
            message.setSuccess(book.getTitle() +" has been deleted from " + category.getName() +".");
        }else{
            message.setError(BOOK_MUST_AT_LEAST_HAVE_ONE_CATEGORY);
        }
        attr.addFlashAttribute("message", message);
        return "redirect:/category/" + category.getId();
    }
}
