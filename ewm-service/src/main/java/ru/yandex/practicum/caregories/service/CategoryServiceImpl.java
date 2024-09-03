package ru.yandex.practicum.caregories.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.caregories.Category;
import ru.yandex.practicum.caregories.CategoryRepository;
import ru.yandex.practicum.caregories.dto.CategoryDto;
import ru.yandex.practicum.caregories.dto.CategoryMapper;
import ru.yandex.practicum.caregories.dto.NewCategoryDto;
import ru.yandex.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.caregories.dto.CategoryMapper.toCategory;
import static ru.yandex.practicum.caregories.dto.CategoryMapper.toCategoryDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("В сервисе создаем категорию");
        Category category = toCategory(newCategoryDto);
        return toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(int catId) {
        log.info("В сервисе удаляем категорию");
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с данным id не найдена");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, int catId) {
        log.info("В сервисе обновляем категорию");
        Category oldCategory = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена"));
        oldCategory.setName(categoryDto.getName());
        return toCategoryDto(categoryRepository.save(oldCategory));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("В сервисе получаем категории");
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(int catId) {
        log.info("В сервисе получаем категорию");
        return toCategoryDto(categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена")));
    }
}