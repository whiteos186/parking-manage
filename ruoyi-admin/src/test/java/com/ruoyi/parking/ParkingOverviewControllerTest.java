package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ruoyi.parking.controller.ParkingOverviewController;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.service.IParkingOverviewService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ParkingOverviewControllerTest
{
    @Test
    void statsEndpointReturnsSuccessPayloadWithBusinessMetrics() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingOverviewStats stats = new ParkingOverviewStats();
        stats.setLotCount(3L);
        stats.setTotalSpaceCount(100L);
        stats.setAvailableSpaceCount(60L);
        stats.setOccupiedSpaceCount(30L);
        stats.setDisabledSpaceCount(10L);
        when(overviewService.selectOverviewStats()).thenReturn(stats);

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/overview/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.lotCount").value(3))
            .andExpect(jsonPath("$.data.totalSpaceCount").value(100))
            .andExpect(jsonPath("$.data.availableSpaceCount").value(60))
            .andExpect(jsonPath("$.data.occupiedSpaceCount").value(30))
            .andExpect(jsonPath("$.data.disabledSpaceCount").value(10));

        verify(overviewService).selectOverviewStats();
    }

    @Test
    void lotListEndpointReturnsTableDataAndBindsFilters() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingLot lot = new ParkingLot();
        lot.setLotId(1L);
        lot.setLotName("Demo Lot");
        lot.setStatus("0");
        when(overviewService.selectParkingLotList(any(ParkingLot.class))).thenReturn(List.of(lot));

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lot/list")
                .param("lotName", "Demo")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].lotId").value(1))
            .andExpect(jsonPath("$.rows[0].lotName").value("Demo Lot"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingLot> captor = ArgumentCaptor.forClass(ParkingLot.class);
        verify(overviewService).selectParkingLotList(captor.capture());
        assertEquals("Demo", captor.getValue().getLotName());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void spaceListEndpointReturnsTableDataAndBindsFilters() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(11L);
        space.setLotId(1L);
        space.setSpaceCode("A1-001");
        space.setStatus("0");
        when(overviewService.selectParkingSpaceList(any(ParkingSpace.class))).thenReturn(Collections.singletonList(space));

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/space/list")
                .param("lotId", "1")
                .param("spaceCode", "A1")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].spaceId").value(11))
            .andExpect(jsonPath("$.rows[0].lotId").value(1))
            .andExpect(jsonPath("$.rows[0].spaceCode").value("A1-001"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingSpace> captor = ArgumentCaptor.forClass(ParkingSpace.class);
        verify(overviewService).selectParkingSpaceList(captor.capture());
        assertEquals(1L, captor.getValue().getLotId());
        assertEquals("A1", captor.getValue().getSpaceCode());
        assertEquals("0", captor.getValue().getStatus());
    }
}
