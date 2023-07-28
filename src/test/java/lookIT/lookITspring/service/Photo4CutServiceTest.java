package lookIT.lookITspring.service;

import lookIT.lookITspring.entity.Collections;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.CollectionsRepository;
import lookIT.lookITspring.repository.LandmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class Photo4CutServiceTest {

    @Autowired
    private LandmarkRepository landmarkRepository;
    @Autowired
    private Photo4CutService photo4CutService;

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
            photo4CutService.Photo4CutDelete(photo4cutId);
            fail("Expected Exception to be thrown, but it was not thrown.");
        }catch(Exception e){
            assertEquals("No collection found for the given photo4CutId.", e.getMessage());
        }
    }

}

