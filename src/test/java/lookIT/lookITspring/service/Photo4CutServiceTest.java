package lookIT.lookITspring.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lookIT.lookITspring.controller.Photo4cutController;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.repository.LandmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.transaction.Transactional;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class Photo4CutServiceTest {

    @Autowired
    private LandmarkRepository landmarkRepository;
    @Autowired
    private Photo4CutService photo4CutService;
    @Autowired
    private Photo4cutController photo4cutController;
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("랜드마크 네컷 프레임 조회 실패 - 존재하지 않는 랜드마크")
    public void getPhotoFrameTestFail_NonExistLandmark() throws Exception{
        try{
            photo4CutService.getPhotoFrame(5000L);
            fail("Expected Exception to be thrown, but it was not thrown.");
        }catch(Exception e){
            assertEquals("No landmark found for the given landmarkId." , e.getMessage());
        }
    }

    @Test
    @DisplayName("랜드마크 네컷 프레임 조회 실패 - 프레임 없는 경우")
    public void getPhotoFrameTestFail_NonExistFrame() throws Exception{
        try{
            Landmark landmark = Landmark.builder()
                    .landmarkName("Test Landmark")
                    .landLatitude(0.0)
                    .landLongitude(0.0)
                    .build();

            landmarkRepository.save(landmark);
            photo4CutService.getPhotoFrame(landmark.getLandmarkId());
            fail("Expected Exception to be thrown, but it was not thrown.");
        }catch(Exception e){
            assertEquals("No landmarkFrame for the given landmark." , e.getMessage());
        }
    }

    @Test
    @DisplayName("랜드마크 네컷 프레임 조회 성공")
    public void getPhotoFrameTestSuccess() throws Exception{
        String expectedFrameUrl = "http://example.com/frame.jpg";
        Landmark landmark = Landmark.builder()
                .landmarkName("Test Landmark")
                .landLatitude(0.0)
                .landLongitude(0.0)
                .frameUrl(expectedFrameUrl)
                .build();
        landmarkRepository.save(landmark);

        String frameUrl = photo4CutService.getPhotoFrame(landmark.getLandmarkId());
        assertEquals(expectedFrameUrl,frameUrl);
    }

    @Test
    @DisplayName("추억네컷 삭제 실패 - 네컷존재X")
    public void Photo4CutDeleteTestFail_NonExistCollection() {
        try{
            Long photo4cutId = 5000L;
            photo4CutService.deletePhoto4Cut(photo4cutId);
            fail("Expected Exception to be thrown, but it was not thrown.");
        }catch(Exception e){
            assertEquals("No collection found for the given photo4CutId.", e.getMessage());
        }
    }

    @Test
    @DisplayName("추억네컷 태그된 친구 리스트 조회 성공")
    public void getTaggedFriendListByPhoto4CutId() throws Exception{
        //Given
        String tagId = "userTagId";
        String email = "user1@gmail.com";
        String password = "memoryRecord123!";
        String nickName = "userName";
        UserJoinRequestDto user = new UserJoinRequestDto(tagId, email, password, nickName);

        String tagId1 = "friendTagId";
        String email1 = "friend@gmail.com";
        String password1 = "memoryRecord123!";
        String nickName1 = "friendName";
        UserJoinRequestDto friend = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
        userService.join(user);
        userService.join(friend);

        HashMap<String, String> user1 = new HashMap<>();
        user1.put("email", email);
        user1.put("password", password);
        String token = userService.login(user1);

        Landmark landmark = Landmark.builder()
            .landmarkName("Test Landmark")
            .landLatitude(0.0)
            .landLongitude(0.0)
            .build();
        landmarkRepository.save(landmark);

        byte[] content = "test file content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.jpeg", "image/jpeg", content);

        MockHttpServletRequest request = new MockHttpServletRequest();
        String imageUrl = "https://example.com/imageUrl_%2023-08-05T16%3A29%3A28.793374800";
        String s3Key = "memoryphoto/test.jpeg";
        request.setAttribute("imageUrl", imageUrl);
        request.setAttribute("s3Key", s3Key);

        Long photo4CutId = photo4cutController.uploadFile(mockMultipartFile,landmark.getLandmarkId(), token, request);

        String[] friendsList = {"friendTagId"};
        photo4CutService.collectionFriendTag(friendsList, photo4CutId);

        //When
        List<Map<String, String>> friendInfo = photo4CutService.getTaggedFriendListByPhoto4CutId(photo4CutId);

        //Then
        assertEquals(nickName1, friendInfo.get(0).get("nickName"));
        assertEquals(tagId1, friendInfo.get(0).get("tagId"));
    }
}

