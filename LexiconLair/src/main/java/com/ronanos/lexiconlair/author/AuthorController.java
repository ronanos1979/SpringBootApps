package com.ronanos.lexiconlair.author;

import java.util.List;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@SessionAttributes("name")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @RequestMapping(value = "list-authors")
    public String listAllAuthors(ModelMap model) {
        List<Author> authorList = authorRepository.findAll();
        model.put("authors", authorList);
        return "listAuthors";
    }

    @RequestMapping(value = "add-author", method = RequestMethod.GET)
    public String showAddNewAuthorPage(ModelMap model) {
        model.addAttribute("author", new Author(0, "", ""));
        return "addAuthor";
    }

    @RequestMapping(value = "add-author", method = RequestMethod.POST)
    public String addNewAuthor(ModelMap model, @Valid @ModelAttribute("author") Author author, BindingResult result) {
        if (result.hasErrors()) {
            return "addAuthor";
        }
        model.addAttribute("author", author);
        authorRepository.save(author);
        logger.info("Created author {} {}", author.getFirstName(), author.getLastName());
        return "redirect:list-authors";
    }

    @PostMapping("delete-author")
    public String deleteAuthor(@RequestParam int id) {
        authorRepository.deleteById(id);
        return "redirect:list-authors";
    }


    @RequestMapping(value = "update-author", method = RequestMethod.GET)
    public String showUpdateAuthorPage(@RequestParam int id, ModelMap model) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        model.addAttribute("author", author);
        return "addAuthor";
    }

    @RequestMapping(value = "update-author", method = RequestMethod.POST)
    public String updateAuthor(ModelMap model, @Valid @ModelAttribute("author") Author author, BindingResult result) {
        if (result.hasErrors()) {
            return "addAuthor";
        }
        model.addAttribute("author", author);
        authorRepository.save(author);
        logger.info("Updated author {} {}", author.getFirstName(), author.getLastName());
        return "redirect:list-authors";
    }
}
