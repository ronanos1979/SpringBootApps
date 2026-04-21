package com.ronanos.lexiconlair.author.web;

import java.util.List;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping("list-authors")
    public String listAllAuthors(ModelMap model) {
        List<Author> authorList = authorRepository.findAll();
        model.addAttribute("authors", authorList);
        return "author/listAuthors";
    }

    @GetMapping("add-author")
    public String showCreateAuthorForm(ModelMap model) {
        model.addAttribute("author", new Author());
        return "author/addAuthor";
    }

    @PostMapping("add-author")
    public String createAuthor(@Valid @ModelAttribute("author") Author author, BindingResult result) {
        if (result.hasErrors()) {
            return "author/addAuthor";
        }

        authorRepository.save(author);
        logger.info("Created author {} {}", author.getFirstName(), author.getLastName());
        return "redirect:list-authors";
    }

    @PostMapping("delete-author")
    public String deleteAuthor(@RequestParam Long id) {
        logger.info("Deleting author with id {}", id);
        authorRepository.deleteById(id);
        return "redirect:list-authors";
    }


    @GetMapping("update-author")
    public String showUpdateAuthorForm(@RequestParam Long id, ModelMap model) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        model.addAttribute("author", author);
        return "author/addAuthor";
    }

    @PostMapping("update-author")
    public String updateAuthor(@Valid @ModelAttribute("author") Author author, BindingResult result) {
        if (result.hasErrors()) {
            return "author/addAuthor";
        }

        authorRepository.save(author);
        logger.info("Updated author {} {}", author.getFirstName(), author.getLastName());
        return "redirect:list-authors";
    }
}
