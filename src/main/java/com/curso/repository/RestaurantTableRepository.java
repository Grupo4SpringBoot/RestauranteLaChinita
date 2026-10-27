package com.curso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.curso.model.RestaurantTable; // Asegúrate de que el import coincida con tu entidad de mesas

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {

    boolean existsByNumber(Integer number);

    boolean existsByNumberAndIdNot(Integer number, Integer id);
}
