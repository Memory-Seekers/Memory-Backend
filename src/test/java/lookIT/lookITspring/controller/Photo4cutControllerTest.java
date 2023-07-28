package lookIT.lookITspring.controller;

import com.amazonaws.services.s3.AmazonS3;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.Collections;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.repository.CollectionsRepository;
import lookIT.lookITspring.repository.LandmarkRepository;
import lookIT.lookITspring.service.MemoryService;
import lookIT.lookITspring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class Photo4cutControllerTest {
    @Autowired
    private UserService userService;
    @Autowired
    private MemoryService memoryService;
    @Autowired
    private LandmarkRepository landmarkRepository;
    @Autowired
    private Photo4cutController photo4cutController;
    @Autowired
    private CollectionsRepository collectionsRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private AmazonS3 s3Client;

    private Long memoryID;
    private String token;
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
        token = userService.login(user1);

        ArrayList<LinePathDto> pathList = new ArrayList<>();
        LinePathDto path1 = new LinePathDto(1.1, 1.2);
        LinePathDto path2 = new LinePathDto(2.1, 2.2);
        pathList.add(path1);
        pathList.add(path2);
        MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);

        memoryID = memoryService.memoryCreate(token, requestDto);

    }

    @Test
    @DisplayName("추억네컷 등록 성공")
    public void uploadFileTestSuccess() throws Exception{
        Landmark landmark = Landmark.builder()
                .landmarkName("Test Landmark")
                .landLatitude(0.0)
                .landLongitude(0.0)
                .build();
        landmarkRepository.save(landmark);

        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        Long photo4cutID = photo4cutController.uploadFile(mockMultipartFile,landmark.getLandmarkId(), token);
        assertNotNull(photo4cutID);

        Collections collection = collectionsRepository.getReferenceById(photo4cutID);
        assertEquals(landmark.getLandmarkId(), collection.getLandmark().getLandmarkId());
    }

    @Test
    @DisplayName("추억네컷 등록 실패 - 존재하지 않는 랜드마크")
    public void uploadFileTestFail() throws Exception{
        try{
            Long landmarkID = 5000L;

            byte[] content = "test file content".getBytes();
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

            Long photo4cutID = photo4cutController.uploadFile(mockMultipartFile,landmarkID, token);
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        }catch(IllegalArgumentException e){
            AssertJUnit.assertEquals("landmark not found" , e.getMessage());
        }

    }

    @Test
    @DisplayName("추억네컷 등록 실패 - 토큰 에러")
    public void uploadFileTestFail_NonExistUser() throws Exception{
        try{
            Long landmarkID = 5000L;

            byte[] content = "test file content".getBytes();
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

            Long photo4cutID = photo4cutController.uploadFile(mockMultipartFile,landmarkID, "nonExistToken");
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        }catch(Exception e){
            assertNotNull(e);
        }

    }

    @Test
    @DisplayName("추억네컷 삭제 성공")
    public void deleteTagTestSuccess() throws Exception{
        Landmark landmark = Landmark.builder()
                .landmarkName("Test Landmark")
                .landLatitude(0.0)
                .landLongitude(0.0)
                .build();
        landmarkRepository.save(landmark);

        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        Long photo4cutID = photo4cutController.uploadFile(mockMultipartFile,landmark.getLandmarkId(), token);
        boolean result = photo4cutController.deleteTag(photo4cutID);
        assertTrue(result);
    }

}