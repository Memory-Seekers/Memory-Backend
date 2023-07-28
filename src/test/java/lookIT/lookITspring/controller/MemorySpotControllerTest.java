package lookIT.lookITspring.controller;

import com.amazonaws.services.s3.AmazonS3;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.MemoryPhoto;
import lookIT.lookITspring.entity.MemorySpot;
import lookIT.lookITspring.repository.MemoryPhotoRepository;
import lookIT.lookITspring.repository.MemorySpotRepository;
import lookIT.lookITspring.service.MemoryService;
import lookIT.lookITspring.service.MemorySpotService;
import lookIT.lookITspring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class MemorySpotControllerTest {
    @Autowired
    private MemorySpotService memorySpotService;
    @Autowired
    private MemorySpotController memorySpotController;
    @Autowired
    private UserService userService;
    @Autowired
    private MemoryService memoryService;
    @Autowired
    private MemorySpotRepository memorySpotRepository;
    @Autowired
    private MemoryPhotoRepository memoryPhotoRepository;
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
    @DisplayName("추억일지 스팟 사진 매칭 성공")
    public void uploadFileTestSuccess() throws Exception{
        Double spotLatitude = 1.1;
        Double spotLongitude = 1.2;
        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        boolean result = memorySpotController.uploadFile(mockMultipartFile, spotLatitude, spotLongitude, memoryID);

        assertEquals(true, result);

        List<MemorySpot> memorySpots = memorySpotRepository.findBySpotLatitudeAndSpotLongitude(spotLatitude, spotLongitude);

        assertNotNull(memorySpots);

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
        assertEquals(memoryID, memory.getMemoryId());

        List<MemoryPhoto> memoryPhotos =  memoryPhotoRepository.findAllByMemorySpot(foundMemorySpot);
        assertEquals(1,memoryPhotos.size());
    }

    @Test
    @DisplayName("추억일지 스팟 사진 매칭 실패 - Invalid memoryID")
    public void uploadFileTestFail_InvalidMemoryID() throws Exception{
        Double spotLatitude = 1.1;
        Double spotLongitude = 1.2;
        Long memoryId = 5000L;
        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        try {
            memorySpotController.uploadFile(mockMultipartFile, spotLatitude, spotLongitude, memoryId);
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        } catch (IllegalArgumentException e) {
            AssertJUnit.assertEquals("Invalid memoryId: " + memoryId, e.getMessage());
        }
    }

    @Test
    @DisplayName("추억일지 사진 삭제 성공")
    public void deletePhotoSuccess() throws IOException {
        Double spotLatitude = 1.1;
        Double spotLongitude = 1.2;
        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        memorySpotController.uploadFile(mockMultipartFile, spotLatitude, spotLongitude, memoryID);
        List<MemorySpot> memorySpots = memorySpotRepository.findBySpotLatitudeAndSpotLongitude(spotLatitude, spotLongitude);
        MemorySpot foundMemorySpot = null;
        for (MemorySpot memorySpot : memorySpots) {
            if (spotLatitude.equals(memorySpot.getSpotLatitude()) && spotLongitude.equals(memorySpot.getSpotLongitude())) {
                foundMemorySpot = memorySpot;
                break;
            }
        }
        List<MemoryPhoto> memoryPhotos =  memoryPhotoRepository.findAllByMemorySpot(foundMemorySpot);
        MemoryPhoto memoryPhoto = memoryPhotoRepository.findByMemorySpotSpotId(foundMemorySpot.getSpotId());
        String photoUrl = memoryPhoto.getMemoryPhoto();
        System.out.println("photourl : "+photoUrl);

        int tIndex = photoUrl.indexOf('T');
        String path = photoUrl.substring(0, tIndex + 1);
        String encodedTimePart = photoUrl.substring(tIndex + 1);
        String decodedTimePart = URLDecoder.decode(encodedTimePart, StandardCharsets.UTF_8);
        String decodedUrl = path + decodedTimePart;
        System.out.println("decodeurl : "+decodedUrl);

        boolean result = memorySpotController.DeleteMemorySpotPhoto(decodedUrl);

        assertEquals(true, result);
    }

}