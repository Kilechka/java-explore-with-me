package ru.yandex.practicum.caregories;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository  extends JpaRepository<Category, Integer> {

    boolean existsByNameAndIdNot(String name, int id);
}