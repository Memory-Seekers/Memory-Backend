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

        // When
        landmarkRepository.save(landmark1);
        landmarkRepository.save(landmark2);

        List<AllLandmarkDto> allLandmarks = landmarkService.allLandmarkInfo();

        // Then
        assertEquals("Assertion failed: Number of Landmark is wrong", 6, allLandmarks.size()); //n+2개

        Landmark checkLandmark1 = landmarkRepository.findByLandmarkName("테스트용 랜드마크1");
        assertEquals("Assertion failed: Landmark1 Latitude mismatch", 18.1111354, checkLandmark1.getLandLatitude());
        assertEquals("Assertion failed: Landmark1 Longitude mismatch", 11.2222226, checkLandmark1.getLandLongitude());

        Landmark checkLandmark2 = landmarkRepository.findByLandmarkName("테스트용 랜드마크2");
        assertEquals("Assertion failed: Landmark2 Latitude mismatch", 19.2468, checkLandmark2.getLandLatitude());
        assertEquals("Assertion failed: Landmark2 Longitude mismatch", 12.333625915, checkLandmark2.getLandLongitude());


    }

    @Test
    @DisplayName("랜드마크_ID로 상세정보 조회 - 존재하는 ID")
    public void 랜드마크_ID로_상세정보_조회() throws Exception {
        //Given
        Landmark landmark = Landmark.builder()
            .landmarkName("테스트용 랜드마크3")
            .landLatitude(18.1354)
            .landLongitude(11.2222226)
            .landInfo("테스트를 위해 만들어진 임시 랜드마크3")
            .landmarkAddress("대한민국 서울 어딘가3")
            .frameUrl("https://unknown3_url.com")
            .build();

        //When
        landmarkRepository.save(landmark);

        //Then
        Landmark checkLandmark = landmarkRepository.findByLandmarkName("테스트용 랜드마크3");
        assertEquals("Assertion failed: Landmark name mismatch", "테스트용 랜드마크3", checkLandmark.getLandmarkName());
        assertEquals("Assertion failed: Landmark info mismatch", "테스트를 위해 만들어진 임시 랜드마크3", checkLandmark.getLandInfo());
        assertEquals("Assertion failed: Landmark url mismatch", "https://unknown3_url.com", checkLandmark.getFrameUrl());
        assertEquals("Assertion failed: Landmark address mismatch", "대한민국 서울 어딘가3", checkLandmark.getLandmarkAddress());
    }


    @Test
    @DisplayName("랜드마크_ID로 상세정보 조회 - 존재하지 않는 ID")
    public void testLandmarkInfoWithInvalidLandmarkId() {
        // Given
        landmarkRepository.deleteAll();
        Long landmarkId = 1L;

        // When
        ResponseEntity<LandmarkInfoDto> actualLandmark = landmarkService.landmarkInfoWithLandmarkId(landmarkId);

        // Then
        assertNull(actualLandmark.getBody(), "Assertion failed: Landmark is not empty");
    }


}
