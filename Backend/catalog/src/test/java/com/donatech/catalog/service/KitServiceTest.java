package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.KitDto;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.repository.KitRepository;
import com.donatech.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitServiceTest {

    @Mock KitRepository kitRepository;
    @Mock ProductRepository productRepository;

    @InjectMocks KitService kitService;

    @Test
    void getAll_returnsList() {
        when(kitRepository.findAll()).thenReturn(List.of(new Kit()));

        List<Kit> result = kitService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_exists_returnsKit() {
        Kit kit = Kit.builder().id(1L).nombre("Kit Agua").build();
        when(kitRepository.findById(1L)).thenReturn(Optional.of(kit));

        Kit result = kitService.getById(1L);

        assertThat(result.getNombre()).isEqualTo("Kit Agua");
    }

    @Test
    void getById_notFound_throwsException() {
        when(kitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validDto_savesKit() {
        KitDto dto = new KitDto();
        dto.setNombre("Kit Alimentos");
        dto.setDescripcion("Descripción");
        dto.setPrecioEstimado(5000);
        dto.setActivo(1);

        when(kitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<MessageResponse> response = kitService.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(kitRepository).save(any(Kit.class));
    }

    @Test
    void update_existingId_updatesKit() {
        Kit kit = Kit.builder().id(1L).nombre("Viejo").items(new ArrayList<>()).build();
        KitDto dto = new KitDto();
        dto.setNombre("Nuevo Nombre");
        dto.setPrecioEstimado(8000);

        when(kitRepository.findById(1L)).thenReturn(Optional.of(kit));
        when(kitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<MessageResponse> response = kitService.update(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(kit.getNombre()).isEqualTo("Nuevo Nombre");
    }

    @Test
    void delete_existingId_deletesKit() {
        Kit kit = Kit.builder().id(1L).build();
        when(kitRepository.findById(1L)).thenReturn(Optional.of(kit));

        ResponseEntity<MessageResponse> response = kitService.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(kitRepository).delete(kit);
    }
}
