package com.donatech.users.service;

import com.donatech.users.dto.ZonaCatastrofeDto;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.ZonaCatastrofe;
import com.donatech.users.repository.ComunaRepository;
import com.donatech.users.repository.RegionRepository;
import com.donatech.users.repository.ZonaCatastrofeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaCatastrofeServiceTest {

    @Mock ZonaCatastrofeRepository zonaRepository;
    @Mock RegionRepository regionRepository;
    @Mock ComunaRepository comunaRepository;

    @InjectMocks ZonaCatastrofeService zonaCatastrofeService;

    @Test
    void getAll_returnsList() {
        when(zonaRepository.findAll()).thenReturn(List.of(new ZonaCatastrofe()));

        List<ZonaCatastrofe> result = zonaCatastrofeService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getActivas_returnsOnlyActiveZones() {
        ZonaCatastrofe activa = new ZonaCatastrofe();
        activa.setActiva(true);
        when(zonaRepository.findByActivaTrue()).thenReturn(List.of(activa));

        List<ZonaCatastrofe> result = zonaCatastrofeService.getActivas();

        assertThat(result).allMatch(z -> Boolean.TRUE.equals(z.getActiva()));
    }

    @Test
    void getById_exists_returnsZona() {
        ZonaCatastrofe zona = new ZonaCatastrofe();
        zona.setId(1L);
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));

        ZonaCatastrofe result = zonaCatastrofeService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_notFound_throwsException() {
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> zonaCatastrofeService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validDto_savesZona() {
        ZonaCatastrofeDto dto = new ZonaCatastrofeDto();
        dto.setRegionId(1L);
        dto.setComunaId(1L);
        dto.setNombreEvento("Terremoto Norte");
        dto.setFechaDeclaracion(LocalDate.now());
        dto.setActiva(true);

        com.donatech.users.model.Region region = new com.donatech.users.model.Region();
        com.donatech.users.model.Comuna comuna = new com.donatech.users.model.Comuna();

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comuna));
        when(zonaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ZonaCatastrofe result = zonaCatastrofeService.create(dto);

        assertThat(result.getNombreEvento()).isEqualTo("Terremoto Norte");
        assertThat(result.getActiva()).isTrue();
        verify(zonaRepository).save(any(ZonaCatastrofe.class));
    }

    @Test
    void update_existingId_updatesFields() {
        ZonaCatastrofe zona = new ZonaCatastrofe();
        zona.setId(1L);
        zona.setActiva(true);
        ZonaCatastrofeDto dto = new ZonaCatastrofeDto();
        dto.setNombreEvento("Inundación Sur");
        dto.setActiva(false);

        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(zonaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ZonaCatastrofe result = zonaCatastrofeService.update(1L, dto);

        assertThat(result.getNombreEvento()).isEqualTo("Inundación Sur");
        assertThat(result.getActiva()).isFalse();
    }

    @Test
    void desactivar_existingId_setsInactive() {
        ZonaCatastrofe zona = new ZonaCatastrofe();
        zona.setActiva(true);
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));

        zonaCatastrofeService.desactivar(1L);

        assertThat(zona.getActiva()).isFalse();
        verify(zonaRepository).save(zona);
    }
}
