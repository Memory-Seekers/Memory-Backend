package lookIT.lookITspring.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.Collections;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.CollectionsRepository;
import lookIT.lookITspring.repository.LandmarkRepository;
import lookIT.lookITspring.repository.UserRepository;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
//@ExtendWith(MockitoExtension.class)
public class Photo4CutServiceTest {

//    @Mock
//    private LandmarkRepository landmarkRepository;
//
//    @InjectMocks
//    private Photo4CutService photo4CutService;
//
//    @Mock
//    private CollectionsRepository collectionsRepository;

    @Autowired Photo4CutService photo4CutService;
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    private AmazonS3 s3Client;

//    @Test
//    public void testGetPhotoFrame() throws Exception {
//        Long landmarkId = 1L;
//        String expectedFrameUrl = "http://example.com/frame.jpg";
//        Landmark landmark = Landmark.builder()
//            .landmarkId(landmarkId)
//            .landmarkName("Test Landmark")
//            .landLatitude(0.0)
//            .landLongitude(0.0)
//            .landInfo("Test Landmark Info")
//            .frameUrl(expectedFrameUrl)
//            .build();
//        when(landmarkRepository.findById(landmarkId)).thenReturn(Optional.of(landmark));
//
//        String result = photo4CutService.getPhotoFrame(landmarkId);
//
//        assertEquals(expectedFrameUrl, result);
//    }
//
//    @Test
//    public void testGetPhotoFrameNoLandmarkFound() throws Exception {
//        Long landmarkId = 1L;
//        when(landmarkRepository.findById(landmarkId)).thenReturn(Optional.empty());
//
//        assertThrows(Exception.class, () -> photo4CutService.getPhotoFrame(landmarkId));
//    }
//
//    @Test
//    public void testGetPhotoFrameNoFrameUrl() throws Exception {
//        Long landmarkId = 1L;
//        Landmark landmark = Landmark.builder()
//            .landmarkId(landmarkId)
//            .landmarkName("Test Landmark")
//            .landLatitude(0.0)
//            .landLongitude(0.0)
//            .landInfo("Test Landmark Info")
//            .build();
//        when(landmarkRepository.findById(landmarkId)).thenReturn(Optional.of(landmark));
//
//        assertThrows(Exception.class, () -> photo4CutService.getPhotoFrame(landmarkId));
//    }
//
//    @Test
//    public void getCollectionsByUserId() {
//        Long userId = 1L;
//        Long landmarkId = 1L;
//        List<Collections> collectionsList = new ArrayList<>();
//        User user = User.builder().userId(userId).build();
//        Landmark landmark = Landmark.builder().landmarkId(landmarkId).build();
//        LocalDateTime now = LocalDateTime.now();
//        Collections collection1 = Collections.builder().photo4CutId(1L).user(user)
//            .landmark(landmark).photo4Cut("test1").createAt(now).build();
//        Collections collection2 = Collections.builder().photo4CutId(2L).user(user)
//            .landmark(landmark).photo4Cut("test2").createAt(now.minusMinutes(10)).build();
//        collectionsList.add(collection1);
//        collectionsList.add(collection2);
//        when(collectionsRepository.findAllByUserIdOrderByCreateAtDesc(userId)).thenReturn(
//            collectionsList);
//
//        List<Collections> result = photo4CutService.getCollectionsByUserId(userId);
//
//        assertThat(result).isEqualTo(collectionsList);
//        verify(collectionsRepository).findAllByUserIdOrderByCreateAtDesc(userId);
//    }

    @Test
    @DisplayName("추억네컷 태그된 친구 리스트 조회_성공")
    public void getTaggedFriendListByPhoto4CutIdIdSuccess() throws Exception {
        //Given
        Long landmarkId = 1L;

        String tagId ="test";
        String email ="test@gmail.com";
        String password ="memoryRecord123!";
        String nickName ="test";
        UserJoinRequestDto user1 = new UserJoinRequestDto(tagId, email, password, nickName);
        userService.join(user1);
        User user = userRepository.findByEmail(email).get();
        Long userId = user.getUserId();

        MultipartFile file = new MockMultipartFile("스마일.jpg", new FileInputStream(new File("").getAbsolutePath() + "/src/main/resources/static/images/스마일.jpg"));
        String fileName = file.getOriginalFilename();
        String folderName = "photo4cut/photo";
        LocalDateTime now = LocalDateTime.now();
        String nowTime = now.toString();
        String key = folderName + "/" + fileName + nowTime;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        PutObjectRequest request = new PutObjectRequest(bucket, key, file.getInputStream(),
            metadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead);
        s3Client.putObject(request);
        String imageUrl = s3Client.getUrl(bucket, key).toString();

        Long photo4CutId = photo4CutService.savePhoto4Cut(landmarkId, userId, imageUrl, key);

        String tagId1 ="testFriend1";
        String email1 ="testFriend1@gmail.com";
        String password1 ="memoryRecord123!";
        String nickName1 ="testFriend1";
        UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
        userService.join(userDto1);

        String tagId2 ="testFriend2";
        String email2 ="testFriend2@gmail.com";
        String password2 ="memoryRecord123!";
        String nickName2 ="testFriend2";
        UserJoinRequestDto userDto2 = new UserJoinRequestDto(tagId2, email2, password2, nickName2);
        userService.join(userDto2);

        String[] friendsList= {"testFriend1", "testFriend2"};
        photo4CutService.collectionFriendTag(friendsList, photo4CutId);

        //When
        List<Map<String,String>> findFriendList = photo4CutService.getTaggedFriendListByPhoto4CutIdId(photo4CutId);

        //Then
        assertEquals(friendsList[0], findFriendList.get(0).get("tagId"));
        assertEquals(friendsList[1], findFriendList.get(1).get("tagId"));
    }
}

