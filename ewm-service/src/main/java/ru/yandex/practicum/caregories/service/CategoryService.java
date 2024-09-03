package ru.yandex.practicum.caregories.service;

import ru.yandex.practicum.caregories.dto.CategoryDto;
import ru.yandex.practicum.caregories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(int catId);

    CategoryDto updateCategory(CategoryDto categoryDto, int catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(int catId);
}