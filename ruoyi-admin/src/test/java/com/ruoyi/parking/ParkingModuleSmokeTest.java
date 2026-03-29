package com.ruoyi.parking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ruoyi.parking.controller.ParkingHealthController;
import com.ruoyi.parking.service.impl.ParkingHealthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ParkingModuleSmokeTest
{
    @Test
    void parkingHealthEndpointReturnsStructuredSuccess() throws Exception
    {
        ParkingHealthController controller = new ParkingHealthController(new ParkingHealthServiceImpl());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.module").value("parking"))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
