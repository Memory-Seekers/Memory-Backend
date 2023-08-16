package lookIT.lookITspring.controller;

import com.amazonaws.services.s3.AmazonS3;
import java.util.Map;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.Collections;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.repository.CollectionsRepository;
import lookIT.lookITspring.repository.LandmarkRepository;
import lookIT.lookITspring.service.MemoryService;
import lookIT.lookITspring.service.Photo4CutService;
import lookIT.lookITspring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.testng.AssertJUnit.assertEquals;

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
    @Autowired
    private Photo4CutService photo4CutService;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private AmazonS3 s3Client;

    private Long memoryID;
    private String token;
    private Long photo4cutID;
    private Long landmarkID;
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
        token = userService.login(user1).getAccessToken();

        ArrayList<LinePathDto> pathList = new ArrayList<>();
        LinePathDto path1 = new LinePathDto(1.1, 1.2);
        LinePathDto path2 = new LinePathDto(2.1, 2.2);
        pathList.add(path1);
        pathList.add(path2);
        MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);

        memoryID = memoryService.createMemory(token, requestDto);

        Landmark landmark = Landmark.builder()
                .landmarkName("Test Landmark")
                .landLatitude(0.0)
                .landLongitude(0.0)
                .build();
        landmarkRepository.save(landmark);

        landmarkID = landmark.getLandmarkId();

        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        MockHttpServletRequest request = new MockHttpServletRequest();
        String imageUrl = "https://example.com/imageUrl_%2023-08-05T16%3A29%3A28.793374800";
        String s3Key = "memoryphoto/test.jpeg";
        request.setAttribute("imageUrl", imageUrl);
        request.setAttribute("s3Key", s3Key);

        photo4cutID = photo4cutController.uploadFile(mockMultipartFile,landmark.getLandmarkId(), token, request);
    }

    @Test
    @DisplayName("추억네컷 등록 성공")
    public void uploadFileTestSuccess() throws Exception{
        assertNotNull(photo4cutID);

        Collections collection = collectionsRepository.getReferenceById(photo4cutID);
        assertEquals(landmarkID, collection.getLandmark().getLandmarkId());
    }

    @Test
    @DisplayName("추억네컷 등록 실패 - 존재하지 않는 랜드마크")
    public void uploadFileTestFail_InvalidLandmark() throws Exception{
        try{
            byte[] content = "test file content".getBytes();
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

            MockHttpServletRequest request = new MockHttpServletRequest();
            String imageUrl = "https://example.com/imageUrl_%2023-08-05T16%3A29%3A28.793374800";
            String s3Key = "memoryphoto/test.jpeg";
            request.setAttribute("imageUrl", imageUrl);
            request.setAttribute("s3Key", s3Key);

            Long photo4cutID = photo4cutController.uploadFile(mockMultipartFile,5000L, token, request);
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        }catch(IllegalArgumentException e){
            AssertJUnit.assertEquals("landmark not found" , e.getMessage());
        }

    }

    @Test
    @DisplayName("추억네컷 등록 실패 - s3Err")
    public void uploadFileTestFail_imgUrlorKeyNull() throws Exception{
        try{
            byte[] content = "test file content".getBytes();
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);
            MockHttpServletRequest request = new MockHttpServletRequest();

            Long photo4cutID = photo4cutController.uploadFile(mockMultipartFile,landmarkID, token, request);
            fail("Expected IllegalArgumentException to be thrown, but it was not thrown.");
        }catch(Exception e){
            AssertJUnit.assertEquals("S3 Err - imageUrl or s3Key is null" , e.getMessage());
        }

    }

    @Test
    @DisplayName("추억네컷 삭제 성공")
    public void deleteTagTestSuccess() throws Exception{
        boolean result = photo4cutController.delete4CutPhoto(photo4cutID);
        assertTrue(result);
    }

    @Test
    @DisplayName("내가 생성한 추억네컷 리스트 조회")
    public void getMyMemory4CutTest() throws Exception{
        byte[] content2 = "test file content".getBytes();
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content2);

        MockHttpServletRequest request = new MockHttpServletRequest();
        String imageUrl = "https://example.com/imageUrl2_%2023-08-05T16%3A29%3A28.793374800";
        String s3Key = "memoryphoto/test2.jpeg";
        request.setAttribute("imageUrl", imageUrl);
        request.setAttribute("s3Key", s3Key);
        photo4cutController.uploadFile(mockMultipartFile2,landmarkID, token, request);

        List<Collections> collections = photo4cutController.getMyMemory4Cut(token);
        assertEquals(2, collections.size());
    }


    @Test
    @DisplayName("태그된 추억네컷 리스트 조회")
    public void getCollectionsByTagIdTestSuccess() throws Exception{
        String tagId2 = "userTagId2";
        String email2 = "user2@gmail.com";
        String password2 = "memoryRecord123!";
        String nickName2 = "userName2";
        UserJoinRequestDto user2 = new UserJoinRequestDto(tagId2, email2, password2, nickName2);
        userService.join(user2);

        String[] friendList = new String[]{tagId2};
        photo4CutService.collectionFriendTag(friendList,photo4cutID);

        List<Collections> collections = photo4CutService.getCollectionsByTagId(tagId2);
        assertEquals(1,collections.size());

        Collections collection = collections.get(0);
        assertEquals(photo4cutID, collection.getPhoto4CutId());
        assertEquals(landmarkID,collection.getLandmark().getLandmarkId());
        assertEquals("user1@gmail.com",collection.getUser().getEmail());
    }

    @Test
    @DisplayName("추억네컷 태그된 친구 리스트 조회 성공")
    public void getTaggedFriendListByPhoto4CutId() throws Exception{
        //Given
        String tagId1 = "friendTagId";
        String email1 = "friend@gmail.com";
        String password1 = "memoryRecord123!";
        String nickName1 = "friendName";
        UserJoinRequestDto friend = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
        userService.join(friend);

        String[] friendsList = {"friendTagId"};
        photo4CutService.collectionFriendTag(friendsList, photo4cutID);

        //When
        List<Map<String, String>> friendInfo = photo4CutService.getTaggedFriendListByPhoto4CutId(photo4cutID);

        //Then
        assertEquals(nickName1, friendInfo.get(0).get("nickName"));
        assertEquals(tagId1, friendInfo.get(0).get("tagId"));
    }
}