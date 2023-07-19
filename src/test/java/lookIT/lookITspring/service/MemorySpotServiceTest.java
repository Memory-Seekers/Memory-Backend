package lookIT.lookITspring.service;

import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.MemoryPhoto;
import lookIT.lookITspring.entity.MemorySpot;
import lookIT.lookITspring.repository.MemoryPhotoRepository;
import lookIT.lookITspring.repository.MemoryRepository;
import lookIT.lookITspring.repository.MemorySpotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.testng.annotations.Test;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

//@JdbcTest
@SpringBootTest
@Transactional
public class MemorySpotServiceTest {

    @Autowired
    MemorySpotRepository memorySpotRepository;

    @Autowired
    MemorySpotService memorySpotService;

    @Autowired
    MemoryPhotoRepository memoryPhotoRepository;
/*

    @Test
    public void testCreateNewMemorySpot() {
        MockitoAnnotations.openMocks(this);

        Double spotLatitude = 37.12345;
        Double spotLongitude = 127.12345;
        Long memoryId = 1L;
        String imageUrl = "http://example.com/image.jpg";

        Memory memory = Memory.builder().memoryId(memoryId).build();
        when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(memory));

        MemorySpotId id = MemorySpotId.builder().memory(memory).spotLatitude(spotLatitude).spotLongitude(spotLongitude).build();
        MemorySpot memorySpot = new MemorySpot(id, imageUrl);
        when(memorySpotRepository.save(memorySpot)).thenReturn(memorySpot);

        boolean result = memorySpotService.createNewMemorySpot(spotLatitude, spotLongitude, memoryId, imageUrl);

        assertTrue(result);
    }
    */

    @BeforeEach
    public void setUp(){
        Long memoryID1 = 1L;
        Long memoryID2 = 2L;
        Memory memory1 = Memory.builder().memoryId(memoryID1).build();
        Memory memory2 = Memory.builder().memoryId(memoryID2).build();

        Double spot1 = 12.3456;
        Double spot2 = 12.34567;
        List<Double> spotList = new ArrayList<>();
        spotList.add(spot1);
        spotList.add(spot2);

        List<MemorySpot> memorySpots = new ArrayList<>();
        int counter = 2;

        for (Double latitudeLongitude : spotList){
            MemorySpot memoryspot = MemorySpot.builder()
                    .spotLatitude(latitudeLongitude)
                    .spotLongitude(latitudeLongitude)
                    .memory(memory1)
                    .build();
            memorySpots.add(memoryspot);

            String imageUrl = "https://look-it.renewal/memoryphoto/%ED%99%example";
            String key = "https://look-it.renewal/memoryphoto/%ED%99%key";

            for (int i=0; i<counter; i++){
                MemoryPhoto memoryPhoto = MemoryPhoto.builder()
                        .memorySpot(memoryspot)
                        .memoryPhoto(imageUrl)
                        .memoryPhotoKey(key)
                        .build();
                memoryPhotoRepository.save(memoryPhoto);
            }
            counter--;
        }
        memorySpotRepository.saveAll(memorySpots);
    }
    @Test
    @DisplayName("특정 추억일지 정보 불러오기 - 핀 있을 때")
    public void testShowAllMemorySpotPhotos() throws Exception {
        Long memoryID = 1L;

        //when
        List<Map<String, Object>> res = memorySpotService.showAllMemorySpotPhotos(memoryID);

        //then
        assertNotNull(res);
        assertEquals(2,res.size());

        assertEquals(12.3456, res.get(0).get("spotLatitude"));
        assertEquals(12.3456, res.get(0).get("spotLongitude"));
        List<String> spot1MemoryPhotos = (List<String>) res.get(0).get("memoryPhotos");
        assertEquals(2, spot1MemoryPhotos.size());
        assertEquals("https://look-it.renewal/memoryphoto/%ED%99%example", spot1MemoryPhotos.get(0));
        assertEquals("https://look-it.renewal/memoryphoto/%ED%99%example", spot1MemoryPhotos.get(1));

        assertEquals(123.12111, res.get(1).get("spotLatitude"));
        assertEquals(121.1221991, res.get(1).get("spotLongitude"));
        List<String> spot2MemoryPhotos = (List<String>) res.get(1).get("memoryPhotos");
        assertEquals(1, spot2MemoryPhotos.size());
        assertEquals("https://look-it.renewal/memoryphoto/%ED%99%example", spot2MemoryPhotos.get(0));
    }
}
