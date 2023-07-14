package lookIT.lookITspring.controller;

import lookIT.lookITspring.service.MemorySpotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
class MemorySpotControllerTest {
    @Mock //Mock 객체 생성
    private MemorySpotService  memorySpotService;

    @InjectMocks // Mock 객체를 자동으로 주입
    private MemorySpotController memorySpotController;

    @Test
    public void testMemoryPhotoEndpoint() throws Exception{
        Long memoryId_1 = 1L;
        Long memoryId_2 = 26L;
        List<Map<String, Object>> expectedRes1 = new ArrayList<>(); // 예상 결과값1 - 추억일지에 spot이 없을 때
        List<Map<String, Object>> expectedRes2 = new ArrayList<>(); // 예상 결과값1 - spot이 있을 때

        // MemorySpotController의 MemoryPhoto() 메서드에서 해당 Mock 메서드가 호출되면 예상 결과를 반환
        // 예상 결과값 1 설정
        when(memorySpotService.showAllMemorySpotPhotos(memoryId_1)).thenReturn(expectedRes1);
        // 예상 결과값 2 설정
        Map<String, Object> spotData = new HashMap<>();
        spotData.put("spotLatitude", 123.12111);
        spotData.put("spotLongitude", 121.1221991);
        spotData.put("memoryPhotos", new ArrayList<>());
        List<Long> spotIDs = new ArrayList<>();
        spotIDs.add(1L);
        spotIDs.add(2L);
        spotData.put("spotIDs", spotIDs);
        expectedRes2.add(spotData);
        when(memorySpotService.showAllMemorySpotPhotos(memoryId_2)).thenReturn(expectedRes2);

        //MemorySpotController에 대한 MockMvc 객체를 생성
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(memorySpotController).build();

        /*memories/photo URL에 memoryId 파라미터를 포함한 GET 요청을 수행
        -> MemorySpotController의 MemoryPhoto() 메서드를 호출
        -> 결과는 MvcResult로 반환됨
         */
        MvcResult mvcResult1 = mockMvc.perform(get("/memories/photo")
                        .param("memoryId", String.valueOf(memoryId_1)))
                .andExpect(status().isOk()) // 응답 상태 200인지 확인
                .andReturn(); // 요청에 대한 결과를 MvcResult로 반환
        MvcResult mvcResult2 = mockMvc.perform(get("/memories/photo")
                        .param("memoryId", String.valueOf(memoryId_2)))
                .andExpect(status().isOk())
                .andReturn();

        // 요청의 URL이 "/memories/photo"인지 확인
        assertThat(mvcResult1.getRequest().getRequestURI()).isEqualTo("/memories/photo");
        assertThat(mvcResult2.getRequest().getRequestURI()).isEqualTo("/memories/photo");
        // http 응답 본문에 json 형태의 유효한 값이 포함되어있는지 확인
        assertThat(mvcResult1.getResponse().getContentAsString()).isNotEmpty().isEqualTo("[]");
        assertThat(mvcResult2.getResponse().getContentAsString()).isNotEmpty().isEqualTo(
                "[{\"spotLatitude\":123.12111,\"spotLongitude\":121.1221991,\"memoryPhotos\":[],\"spotIDs\":[1,2]}]");
        // 예상한대로 비즈니스 로직이 불렸는지 확인 - 메서드가 주어진 memoryId로 호출되었는지?
        verify(memorySpotService).showAllMemorySpotPhotos(memoryId_1);
        verify(memorySpotService).showAllMemorySpotPhotos(memoryId_2);
    }
}