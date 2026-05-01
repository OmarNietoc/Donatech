package com.donatech.catalog.service;

import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Unit;
import com.donatech.catalog.repository.ProductRepository;
import com.donatech.catalog.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;

    public Unit getUnitById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada: " + id));
    }

    public Unit getUnitByName(String name) {
        return unitRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada: " + name));
    }

    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    public boolean validateUnitByName(String name) {
        Optional<Unit> existingUnit = unitRepository.findByNameIgnoreCase(name);
        if (existingUnit.isPresent()) {
            throw new IllegalArgumentException("Ya existe una unidad con ese nombre.");
        }
        return true;
    }

    public void createUnit(Unit unit) {
        validateUnitByName(unit.getName());
        unitRepository.save(unit);
    }

    public void updateUnit(Long id, Unit updatedUnit) {
        Unit existingUnit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada: " + id));
        validateUnitByName(updatedUnit.getName());
        existingUnit.setName(updatedUnit.getName());
        unitRepository.save(existingUnit);
    }

    public void deleteUnit(Long id) {
        getUnitById(id);
        if (productRepository.existsProductsByUnidId(id)) {
            throw new IllegalStateException("No se puede eliminar la unidad porque hay productos asociados.");
        }
        unitRepository.deleteById(id);
    }
}
