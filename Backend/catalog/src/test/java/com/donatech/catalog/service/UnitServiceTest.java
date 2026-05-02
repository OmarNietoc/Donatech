package com.donatech.catalog.service;

import com.donatech.catalog.model.Unit;
import com.donatech.catalog.repository.ProductRepository;
import com.donatech.catalog.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UnitService unitService;

    @Test
    void getAllUnits_ShouldReturnRepositoryPayload() {
        List<Unit> units = List.of(Unit.builder().id(1L).name("Caja").build());
        when(unitRepository.findAll()).thenReturn(units);

        List<Unit> result = unitService.getAllUnits();

        assertSame(units, result);
        verify(unitRepository).findAll();
    }

    @Test
    void createUnit_WithUniqueName_ShouldPersistEntity() {
        Unit unit = Unit.builder().name("Paquete").build();
        when(unitRepository.findByNameIgnoreCase(unit.getName())).thenReturn(Optional.empty());

        unitService.createUnit(unit);

        verify(unitRepository).save(unit);
    }

    @Test
    void updateUnit_WithUniqueName_ShouldUpdateEntity() {
        Long id = 7L;
        Unit existing = Unit.builder().id(id).name("Litro").build();
        Unit updated = Unit.builder().name("Galón").build();

        when(unitRepository.findById(id)).thenReturn(Optional.of(existing));
        when(unitRepository.findByNameIgnoreCase(updated.getName())).thenReturn(Optional.empty());

        unitService.updateUnit(id, updated);

        assertEquals("Galón", existing.getName());
        verify(unitRepository).save(existing);
    }

    @Test
    void deleteUnit_WhenNoProductsAssigned_ShouldDelete() {
        Long id = 2L;
        Unit existing = Unit.builder().id(id).name("Bolsa").build();

        when(unitRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.existsProductsByUnidId(id)).thenReturn(false);

        unitService.deleteUnit(id);

        verify(unitRepository).deleteById(id);
    }
}
