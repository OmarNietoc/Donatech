package com.donatech.catalog.service;

import com.donatech.catalog.client.UsersClient;
import com.donatech.catalog.client.UserStatusDto;
import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.CampaignKitDto;
import com.donatech.catalog.dto.CampaignRequestDto;
import com.donatech.catalog.dto.response.CampaignResponseDto;
import com.donatech.catalog.event.CampaignCreatedPublisher;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Campaign;
import com.donatech.catalog.model.CampaignKit;
import com.donatech.catalog.model.CampaignStatus;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.repository.CampaignKitRepository;
import com.donatech.catalog.repository.CampaignRepository;
import com.donatech.catalog.repository.KitRepository;
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
class CampaignServiceTest {

    @Mock CampaignRepository campaignRepository;
    @Mock CampaignKitRepository campaignKitRepository;
    @Mock KitRepository kitRepository;
    @Mock CampaignCreatedPublisher campaignCreatedPublisher;
    @Mock UsersClient usersClient;

    @InjectMocks CampaignService campaignService;

    @Test
    void getAll_returnsCampaigns() {
        when(campaignRepository.findAll()).thenReturn(List.of(Campaign.builder().kits(new ArrayList<>()).build()));

        List<CampaignResponseDto> result = campaignService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllActive_returnsOnlyActiveCampaigns() {
        Campaign active = Campaign.builder().estado(CampaignStatus.ACTIVA).kits(new ArrayList<>()).build();
        when(campaignRepository.findByEstado(CampaignStatus.ACTIVA)).thenReturn(List.of(active));

        List<CampaignResponseDto> result = campaignService.getAllActive();

        assertThat(result).allMatch(c -> c.getEstado() == CampaignStatus.ACTIVA);
    }

    @Test
    void getById_exists_returnsCampaign() {
        Campaign campaign = Campaign.builder().id(1L).titulo("Campaña Terremoto").kits(new ArrayList<>()).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        CampaignResponseDto result = campaignService.getById(1L);

        assertThat(result.getTitulo()).isEqualTo("Campaña Terremoto");
    }

    @Test
    void getById_notFound_throwsException() {
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validDto_savesAndPublishesEvent() {
        CampaignRequestDto dto = new CampaignRequestDto();
        dto.setBeneficiaryId(1L);
        dto.setTitulo("Ayuda Inundaciones");
        dto.setDescripcion("Descripción");
        dto.setMotivo("Emergencia");
        dto.setRegionId(1L);
        dto.setComunaId(1L);

        when(usersClient.getUserById(1L)).thenReturn(new UserStatusDto(1L, 1));
        when(campaignRepository.existsByBeneficiaryIdAndEstadoIn(any(), any())).thenReturn(false);
        when(campaignRepository.save(any())).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId(10L);
            if (c.getKits() == null) c.setKits(new ArrayList<>());
            return c;
        });

        ResponseEntity<MessageResponse> response = campaignService.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(campaignCreatedPublisher).publishCampaignCreated(any());
    }

    @Test
    void addKit_validData_addsKitToCampaign() {
        Campaign campaign = Campaign.builder().id(1L).kits(new ArrayList<>()).build();
        Kit kit = Kit.builder().id(5L).nombre("Kit Emergencia").build();
        CampaignKitDto dto = new CampaignKitDto();
        dto.setKitId(5L);
        dto.setCantidadNecesaria(10);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignKitRepository.findByCampaignIdAndKitId(1L, 5L)).thenReturn(Optional.empty());
        when(kitRepository.findById(5L)).thenReturn(Optional.of(kit));
        when(campaignRepository.save(any())).thenReturn(campaign);

        ResponseEntity<MessageResponse> response = campaignService.addKit(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(campaign.getKits()).hasSize(1);
    }

    @Test
    void removeKit_existingKit_removesFromCampaign() {
        CampaignKit ck = new CampaignKit();
        when(campaignKitRepository.findByCampaignIdAndKitId(1L, 5L)).thenReturn(Optional.of(ck));

        ResponseEntity<MessageResponse> response = campaignService.removeKit(1L, 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(campaignKitRepository).delete(ck);
    }

    @Test
    void close_activeCampaign_setsFinalizadaStatus() {
        Campaign campaign = Campaign.builder().id(1L).estado(CampaignStatus.ACTIVA).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<MessageResponse> response = campaignService.close(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(campaign.getEstado()).isEqualTo(CampaignStatus.FINALIZADA);
    }
}
