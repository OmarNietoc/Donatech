package com.donatech.users.service;

import com.donatech.users.dto.ZonaCatastrofeDto;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.ZonaCatastrofe;
import com.donatech.users.repository.ComunaRepository;
import com.donatech.users.repository.RegionRepository;
import com.donatech.users.repository.ZonaCatastrofeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaCatastrofeService {

    private final ZonaCatastrofeRepository zonaRepository;
    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;

    public List<ZonaCatastrofe> getAll() {
        return zonaRepository.findAll();
    }

    public List<ZonaCatastrofe> getActivas() {
        return zonaRepository.findByActivaTrue();
    }

    public ZonaCatastrofe getById(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada: " + id));
    }

    public ZonaCatastrofe create(ZonaCatastrofeDto dto) {
        ZonaCatastrofe zona = new ZonaCatastrofe();
        zona.setRegion(regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada: " + dto.getRegionId())));
        zona.setComuna(comunaRepository.findById(dto.getComunaId())
                .orElseThrow(() -> new ResourceNotFoundException("Comuna no encontrada: " + dto.getComunaId())));
        zona.setNombreEvento(dto.getNombreEvento());
        zona.setFechaDeclaracion(dto.getFechaDeclaracion());
        zona.setFechaFin(dto.getFechaFin());
        zona.setActiva(dto.getActiva() != null ? dto.getActiva() : true);
        return zonaRepository.save(zona);
    }

    public ZonaCatastrofe update(Long id, ZonaCatastrofeDto dto) {
        ZonaCatastrofe zona = getById(id);
        zona.setNombreEvento(dto.getNombreEvento());
        zona.setFechaDeclaracion(dto.getFechaDeclaracion());
        zona.setFechaFin(dto.getFechaFin());
        zona.setActiva(dto.getActiva());
        return zonaRepository.save(zona);
    }

    public void desactivar(Long id) {
        ZonaCatastrofe zona = getById(id);
        zona.setActiva(false);
        zonaRepository.save(zona);
    }

    public void delete(Long id) {
        getById(id);
        zonaRepository.deleteById(id);
    }
}
