package com.ronanos.lexiconlair.author;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;

@Controller
@SessionAttributes("name")
public class AuthorController {

    private AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        super();
        this.authorRepository = authorRepository;
    }

    @RequestMapping(value="list-authors")
    public String listAllAuthors(ModelMap model) {
        String userName = getLoggedInUsername(model);
        List<Author> authorList = authorRepository.findAll();

        model.put("authors", authorList);
        return "listAuthors";
    }

    @RequestMapping(value="add-author", method=RequestMethod.GET)
    public String showAddNewAuthorPage(ModelMap model) {
        String userName = getLoggedInUsername(model);

        Author author = new Author(0, "", "");
        model.addAttribute("author", author);
        return "addAuthor";
    }

    @RequestMapping(value="add-author", method=RequestMethod.POST)
    public String addNewAuthor(ModelMap model, @Valid Author author, BindingResult result) {
        System.out.println("Incoming Author: " + author);
        if (result.hasErrors()) {
            return "addAuthor";
        }
        String username = getLoggedInUsername(model);
        model.addAttribute("author", author);
        authorRepository.save(author);
        return "redirect:list-authors";
    }

    @RequestMapping(value="delete-author")
    public String deleteAuthor(int id) {
        authorRepository.deleteById(id);
        return "redirect:list-authors";
    }


    @RequestMapping(value="update-author", method=RequestMethod.GET)
    public String showUpdateuthorPage(@RequestParam int id, ModelMap model) {
        String userName = getLoggedInUsername(model);

        Author author = authorRepository.findById(id).get();
        model.addAttribute("author", author);
        return "addAuthor";
    }

    @RequestMapping(value="update-author", method=RequestMethod.POST)
    public String updateAuthor(ModelMap model, @Valid Author author, BindingResult result) {
        System.out.println("Incoming Author: " + author);
        if (result.hasErrors()) {
            return "addAuthor";
        }
        String username = getLoggedInUsername(model);
        model.addAttribute("author", author);
        authorRepository.save(author);
        return "redirect:list-authors";
    }

    private String getLoggedInUsername(ModelMap model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
