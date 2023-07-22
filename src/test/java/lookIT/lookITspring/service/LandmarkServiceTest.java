package lookIT.lookITspring.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import java.util.List;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.AllLandmarkDto;
import lookIT.lookITspring.dto.LandmarkInfoDto;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.repository.LandmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@Transactional
public class LandmarkServiceTest {

    @Autowired
    LandmarkRepository landmarkRepository;

    @Autowired
    LandmarkService landmarkService;

    @Test
    @DisplayName("랜드마크 리스트 조회")
    public void 랜드마크_리스트_조회() throws Exception {
        // Given
        Integer size = landmarkRepository.findAllLandmarks().size();

        Landmark landmark1 = Landmark.builder()
            .landmarkName("테스트용 랜드마크1")
            .landLatitude(18.1111354)
            .landLongitude(11.2222226)
            .landInfo("테스트를 위해 만들어진 임시 랜드마크1")
            .frameUrl("https://unknown1_url.com")
            .landmarkAddress("대한민국 서울 어딘가1")
            .build();

        Landmark landmark2 = Landmark.builder()
            .landmarkName("테스트용 랜드마크2")
            .landLatitude(19.2468)
            .landLongitude(12.333625915)
            .landInfo("테스트를 위해 만들어진 임시 랜드마크2")
            .frameUrl("https://unknown2_url.com")
            .landmarkAddress("대한민국 서울 어딘가2")
            .build();

        landmarkRepository.save(landmark1);
        landmarkRepository.save(landmark2);

        // When
        AllLandmarkDto checkLandmark1 = landmarkService.allLandmarkInfo().get(size);
        AllLandmarkDto checkLandmark2 = landmarkService.allLandmarkInfo().get(size+1);

        // Then
        assertEquals("Assertion failed: Landmark1 Latitude mismatch", 18.1111354, checkLandmark1.getLandLatitude());
        assertEquals("Assertion failed: Landmark1 Longitude mismatch", 11.2222226, checkLandmark1.getLandLongitude());

        assertEquals("Assertion failed: Landmark2 Latitude mismatch", 19.2468, checkLandmark2.getLandLatitude());
        assertEquals("Assertion failed: Landmark2 Longitude mismatch", 12.333625915, checkLandmark2.getLandLongitude());

    }

    @Test
    @DisplayName("랜드마크_ID로 상세정보 조회 - 존재하는 ID")
    public void 랜드마크_ID로_상세정보_조회() throws Exception {
        //Given
        Landmark landmark = Landmark.builder()
            .landmarkName("테스트용 랜드마크1")
            .landLatitude(18.1111354)
            .landLongitude(11.2222226)
            .landInfo("테스트를 위해 만들어진 임시 랜드마크1")
            .frameUrl("https://unknown1_url.com")
            .landmarkAddress("대한민국 서울 어딘가1")
            .build();

        //When
        landmarkRepository.save(landmark);

        //Then
        ResponseEntity<LandmarkInfoDto> checkLandmark = landmarkService.landmarkInfoWithLandmarkId(landmark.getLandmarkId());
        assertEquals("Assertion failed: Landmark name mismatch", "테스트용 랜드마크1", checkLandmark.getBody().getLandmarkName());
        assertEquals("Assertion failed: Landmark info mismatch", "테스트를 위해 만들어진 임시 랜드마크1", checkLandmark.getBody().getLandInfo());
        assertEquals("Assertion failed: Landmark url mismatch", "https://unknown1_url.com", checkLandmark.getBody().getFrameUrl());
        assertEquals("Assertion failed: Landmark address mismatch", "대한민국 서울 어딘가1", checkLandmark.getBody().getLandmarkAddress());
    }

}
