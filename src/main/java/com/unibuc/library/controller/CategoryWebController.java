package com.unibuc.library.controller;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.model.Category;
import com.unibuc.library.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryWebController {

    private final CategoryService categoryService;

    public CategoryWebController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(@RequestParam(required = false) String name, Model model) {
        List<Category> categories;
        boolean searching = name != null && !name.isBlank();
        if (searching) {
            categories = categoryService.getAllCategories().stream()
                    .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        } else {
            categories = categoryService.getAllCategories();
        }
        model.addAttribute("categories", categories);
        model.addAttribute("searchName", name);
        model.addAttribute("searching", searching);
        return "categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add New Category");
        return "categories/form";
    }

    @PostMapping("/new")
    public String createCategory(@Valid @ModelAttribute("category") Category category,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Category");
            return "categories/form";
        }
        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
            return "redirect:/categories";
        } catch (DuplicateResourceException e) {
            result.rejectValue("name", "name.duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Add New Category");
            return "categories/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        model.addAttribute("pageTitle", "Edit Category");
        return "categories/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("category") Category category,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Category");
            return "categories/form";
        }
        try {
            categoryService.updateCategory(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            return "redirect:/categories";
        } catch (DuplicateResourceException e) {
            result.rejectValue("name", "name.duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Edit Category");
            return "categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (ResourceInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}
