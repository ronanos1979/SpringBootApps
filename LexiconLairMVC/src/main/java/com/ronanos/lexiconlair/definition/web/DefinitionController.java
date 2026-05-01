package com.ronanos.lexiconlair.definition.web;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DefinitionController {
    private DefinitionRepository definitionRepository;

    public DefinitionController(DefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @GetMapping("list-definitions")
    public String listAllDefinitions(ModelMap model) {
        List<Definition> definitionList =  definitionRepository.findAll();
        model.addAttribute("definitions", definitionList);
        return "definition/listDefinitions";
    }

}
