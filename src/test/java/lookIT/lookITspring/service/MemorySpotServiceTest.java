package lookIT.lookITspring.service;

import com.amazonaws.services.s3.AmazonS3;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.LinePath;
import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.MemoryPhoto;
import lookIT.lookITspring.entity.MemorySpot;
import lookIT.lookITspring.repository.LinePathRepository;
import lookIT.lookITspring.repository.MemoryPhotoRepository;
import lookIT.lookITspring.repository.MemoryRepository;
import lookIT.lookITspring.repository.MemorySpotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

//@JdbcTest
@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class MemorySpotServiceTest {
    @Autowired
    private MemorySpotRepository memorySpotRepository;
    @Autowired
    private MemorySpotService memorySpotService;
    @Autowired
    private MemoryPhotoRepository memoryPhotoRepository;
    @Autowired
    private MemoryService memoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private LinePathRepository linePathRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private AmazonS3 s3Client;

    private Long memoryID;
    @BeforeEach
    void setUp() throws Exception{
        String tagId = "userTagId";
        String email = "user1@gmail.com";
        String password = "memoryRecord123!";
        String nickName = "userName";
        UserJoinRequestDto user = new UserJoinRequestDto(tagId, email, password, nickName);
        userService.join(user);

        HashMap<String, String> user1 = new HashMap<>();
        user1.put("email", email);
        user1.put("password", password);
        String token1 = userService.login(user1);

        ArrayList<LinePathDto> pathList = new ArrayList<>();
        LinePathDto path1 = new LinePathDto(1.1, 1.2);
        LinePathDto path2 = new LinePathDto(2.1, 2.2);
        pathList.add(path1);
        pathList.add(path2);
        MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);

        memoryID = memoryService.memoryCreate(token1, requestDto);
    }

    @Test
    @DisplayName("추억일지 스팟 생성(사진 매칭) 성공- valid memoryID")
    public void createNewMemorySpotTest_Success(){

        Double spotLatitude = 12.1213;
        Double spotLongitude = 12.1212;
        Long memoryId = memoryID;
        String imageUrl = "http://look-it.com/images%ED%url";
        String key = "http://look-it.com/images%ED%key";

        // 테스트 실행
        boolean result = memorySpotService.createNewMemorySpot(spotLatitude, spotLongitude, memoryId, imageUrl, key);

        // 테스트 검증
        assertTrue(result);

        List<MemorySpot> memorySpots = memorySpotRepository.findBySpotLatitudeAndSpotLongitude(spotLatitude, spotLongitude);

        assertNotNull(memorySpots);
        assertFalse(memorySpots.isEmpty());

        MemorySpot foundMemorySpot = null;
        for (MemorySpot memorySpot : memorySpots) {
            if (spotLatitude.equals(memorySpot.getSpotLatitude()) && spotLongitude.equals(memorySpot.getSpotLongitude())) {
                foundMemorySpot = memorySpot;
                break;
            }
        }

        assertNotNull(foundMemorySpot);
        assertEquals(spotLatitude, foundMemorySpot.getSpotLatitude());
        assertEquals(spotLongitude, foundMemorySpot.getSpotLongitude());

        Memory memory = foundMemorySpot.getMemory();
        assertNotNull(memory);
        assertEquals(memoryId, memory.getMemoryId());

        MemoryPhoto memoryPhoto = memoryPhotoRepository.findByMemoryPhoto(imageUrl);
        assertNotNull(memoryPhoto);
        assertEquals(imageUrl, memoryPhoto.getMemoryPhoto());
        assertEquals(key, memoryPhoto.getMemoryPhotoKey());
    }

    @Test
    @DisplayName("추억일지 스팟 생성(사진 매칭) 실패 - Invalid memoryID")
    public void createNewMemorySpotTest_Exception() throws Exception{
        Double spotLatitude = 12.1213;
        Double spotLongitude = 12.1212;
        Long memoryId = 5000L;
        String imageUrl = "http://look-it.com/images%ED%url";
        String key = "http://look-it.com/images%ED%key";

        try {
            memorySpotService.createNewMemorySpot(spotLatitude, spotLongitude, memoryId, imageUrl, key);
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid memoryId: " + memoryId, e.getMessage());
        }
    }

    @Test
    @DisplayName("특정 추억일지 정보 불러오기 - 핀 있을 때")
    public void testShowAllMemorySpotPhotosWithSpots() throws Exception {
        // Given
        Long memoryId = memoryID;
        Memory memory = Memory.builder().memoryId(memoryId).build();

        Double spot1 = 12.3456;
        Double spot2 = 12.34567;

        String imageUrl1 = "http://look-it.com/images%ED%url";
        String key1 = "http://look-it.com/images%ED%key";
        String imageUrl2 = "http://look-it.com/images%ED%url2";
        String key2 = "http://look-it.com/images%ED%key2";

        // Save MemorySpots and MemoryPhotos
        memorySpotService.createNewMemorySpot(spot1, spot1, memoryId, imageUrl1, key1);
        memorySpotService.createNewMemorySpot(spot1, spot1, memoryId, imageUrl2, key2);
        memorySpotService.createNewMemorySpot(spot2, spot2, memoryId, imageUrl1, key1);

        // When
        List<Map<String, Object>> res = memorySpotService.showAllMemorySpotPhotos(memoryId);

        // Then
        assertNotNull(res);
        assertEquals(2, res.size());

        assertEquals(spot1, res.get(0).get("spotLatitude"));
        assertEquals(spot1, res.get(0).get("spotLongitude"));
        List<String> spot1MemoryPhotos = (List<String>) res.get(0).get("memoryPhotos");
        assertEquals(2, spot1MemoryPhotos.size());
        assertEquals(imageUrl1, spot1MemoryPhotos.get(0));
        assertEquals(imageUrl2, spot1MemoryPhotos.get(1));

        assertEquals(spot2, res.get(1).get("spotLatitude"));
        assertEquals(spot2, res.get(1).get("spotLongitude"));
        List<String> spot2MemoryPhotos = (List<String>) res.get(1).get("memoryPhotos");
        assertEquals(1, spot2MemoryPhotos.size());
        assertEquals(imageUrl1, spot2MemoryPhotos.get(0));
    }

    @Test
    @DisplayName("특정 추억일지 정보 불러오기 - 핀 없을 때")
    public void testShowAllMemorySpotPhotosWithoutSpots() throws Exception{
        Long memoryId = memoryID;
        Memory memory = Memory.builder().memoryId(memoryId).build();

        // When
        List<Map<String, Object>> res = memorySpotService.showAllMemorySpotPhotos(memoryId);

        // Then
        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("특정 추억일지 경로 조회 성공")
    public void showAllLinePathTest(){
        Long memoryId = memoryID;

        List<LinePath> linePaths = memorySpotService.showAllLinePath(memoryID);

        assertNotNull(linePaths);
        assertEquals(2, linePaths.size());

        assertEquals(1.1, linePaths.get(0).getLatitude());
        assertEquals(1.2, linePaths.get(0).getLongitude());
        assertEquals(memoryID, linePaths.get(0).getMemory().getMemoryId());

        assertEquals(2.1, linePaths.get(1).getLatitude());
        assertEquals(2.2, linePaths.get(1).getLongitude());
        assertEquals(memoryID, linePaths.get(1).getMemory().getMemoryId());
    }

    @Test
    @DisplayName("특정 추억일지 경로 조회 실패 - Invalid memoryID")
    public void showAllLinePathTest_Exception() {
        Long memoryId = 5000L;

        try {
            memorySpotService.showAllLinePath(memoryId);
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid memoryId" , e.getMessage());
        }
    }

}
