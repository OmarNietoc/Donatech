package com.donatech.catalog.service;

import com.donatech.catalog.client.UsersClient;
import com.donatech.catalog.client.UserStatusDto;
import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.CampaignKitDto;
import com.donatech.catalog.dto.CampaignRequestDto;
import com.donatech.catalog.dto.response.CampaignKitResponseDto;
import com.donatech.catalog.dto.response.CampaignResponseDto;
import com.donatech.catalog.event.CampaignCreatedEvent;
import com.donatech.catalog.event.CampaignCreatedPublisher;
import com.donatech.catalog.exception.ConflictException;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Campaign;
import com.donatech.catalog.model.CampaignKit;
import com.donatech.catalog.model.CampaignStatus;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.repository.CampaignKitRepository;
import com.donatech.catalog.repository.CampaignRepository;
import com.donatech.catalog.repository.KitRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignKitRepository campaignKitRepository;
    private final KitRepository kitRepository;
    private final CampaignCreatedPublisher campaignCreatedPublisher;
    private final UsersClient usersClient;

    public List<CampaignResponseDto> getAll() {
        return campaignRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<CampaignResponseDto> getAllActive() {
        return campaignRepository.findByEstado(CampaignStatus.ACTIVA).stream().map(this::toDto).toList();
    }

    public List<CampaignResponseDto> getByBeneficiary(Long beneficiaryId) {
        return campaignRepository.findByBeneficiaryId(beneficiaryId).stream().map(this::toDto).toList();
    }

    public CampaignResponseDto getById(Long id) {
        return toDto(findById(id));
    }

    private Campaign findById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada: " + id));
    }

    private CampaignResponseDto toDto(Campaign c) {
        List<CampaignKitResponseDto> kits = c.getKits().stream().map(ck -> CampaignKitResponseDto.builder()
                .id(ck.getId())
                .kitId(ck.getKit() != null ? ck.getKit().getId() : null)
                .kitNombre(ck.getKit() != null ? ck.getKit().getNombre() : null)
                .kitPrecioEstimado(ck.getKit() != null ? ck.getKit().getPrecioEstimado() : null)
                .cantidadNecesaria(ck.getCantidadNecesaria())
                .cantidadFulfilled(ck.getCantidadFulfilled())
                .build()).toList();

        return CampaignResponseDto.builder()
                .id(c.getId())
                .beneficiaryId(c.getBeneficiaryId())
                .titulo(c.getTitulo())
                .descripcion(c.getDescripcion())
                .motivo(c.getMotivo())
                .estado(c.getEstado())
                .regionId(c.getRegionId())
                .comunaId(c.getComunaId())
                .fechaCreacion(c.getFechaCreacion())
                .fechaActivacion(c.getFechaActivacion())
                .fechaCierre(c.getFechaCierre())
                .observaciones(c.getObservaciones())
                .motivoRechazo(c.getMotivoRechazo())
                .kits(kits)
                .build();
    }

    public ResponseEntity<MessageResponse> create(@Valid CampaignRequestDto dto) {
        UserStatusDto user = usersClient.getUserById(dto.getBeneficiaryId());
        if (user == null || user.status() == null || user.status() != 1) {
            throw new IllegalArgumentException("El usuario no está activo para crear campañas.");
        }

        boolean hasActiveCampaign = campaignRepository.existsByBeneficiaryIdAndEstadoIn(
                dto.getBeneficiaryId(),
                List.of(CampaignStatus.ACTIVA, CampaignStatus.EN_VALIDACION)
        );
        if (hasActiveCampaign) {
            throw new ConflictException("El beneficiario ya tiene una campaña activa o en validación.");
        }

        Campaign campaign = Campaign.builder()
                .beneficiaryId(dto.getBeneficiaryId())
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .motivo(dto.getMotivo())
                .regionId(dto.getRegionId())
                .comunaId(dto.getComunaId())
                .observaciones(dto.getObservaciones())
                .estado(CampaignStatus.EN_VALIDACION)
                .fechaCreacion(LocalDateTime.now())
                .build();

        campaignRepository.save(campaign);

        if (dto.getKits() != null) {
            for (CampaignKitDto kitDto : dto.getKits()) {
                addKitToCampaign(campaign, kitDto);
            }
            campaignRepository.save(campaign);
        }

        campaignCreatedPublisher.publishCampaignCreated(new CampaignCreatedEvent(
                campaign.getId(),
                campaign.getBeneficiaryId(),
                campaign.getTitulo(),
                campaign.getMotivo(),
                campaign.getFechaCreacion()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Campaña creada y enviada a validación. ID: " + campaign.getId()));
    }

    public ResponseEntity<MessageResponse> addKit(Long campaignId, @Valid CampaignKitDto dto) {
        Campaign campaign = findById(campaignId);

        campaignKitRepository.findByCampaignIdAndKitId(campaignId, dto.getKitId()).ifPresent(existing -> {
            throw new IllegalArgumentException("El kit " + dto.getKitId() + " ya está en la campaña.");
        });

        addKitToCampaign(campaign, dto);
        campaignRepository.save(campaign);
        return ResponseEntity.ok(new MessageResponse("Kit agregado a la campaña correctamente."));
    }

    public ResponseEntity<MessageResponse> removeKit(Long campaignId, Long kitId) {
        CampaignKit ck = campaignKitRepository.findByCampaignIdAndKitId(campaignId, kitId)
                .orElseThrow(() -> new ResourceNotFoundException("Kit " + kitId + " no encontrado en la campaña " + campaignId));
        campaignKitRepository.delete(ck);
        return ResponseEntity.ok(new MessageResponse("Kit eliminado de la campaña correctamente."));
    }

    public ResponseEntity<MessageResponse> close(Long campaignId) {
        Campaign campaign = findById(campaignId);
        campaign.setEstado(CampaignStatus.FINALIZADA);
        campaign.setFechaCierre(LocalDateTime.now());
        campaignRepository.save(campaign);
        return ResponseEntity.ok(new MessageResponse("Campaña finalizada correctamente."));
    }

    private void addKitToCampaign(Campaign campaign, CampaignKitDto dto) {
        Kit kit = kitRepository.findById(dto.getKitId())
                .orElseThrow(() -> new ResourceNotFoundException("Kit no encontrado: " + dto.getKitId()));
        CampaignKit ck = CampaignKit.builder()
                .campaign(campaign)
                .kit(kit)
                .cantidadNecesaria(dto.getCantidadNecesaria())
                .build();
        campaign.getKits().add(ck);
    }
}
