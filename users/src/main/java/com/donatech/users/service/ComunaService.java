package com.donatech.users.service;

import com.donatech.users.model.Comuna;
import com.donatech.users.repository.ComunaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComunaService {

    private final ComunaRepository comunaRepository;

    public ComunaService(ComunaRepository comunaRepository) {
        this.comunaRepository = comunaRepository;
    }

    public List<Comuna> getByRegion(Long regionId) {
        return comunaRepository.findByRegionId(regionId);
    }
}
