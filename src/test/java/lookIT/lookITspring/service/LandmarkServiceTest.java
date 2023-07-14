package lookIT.lookITspring.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.AllLandmarkDto;
import lookIT.lookITspring.dto.LandmarkInfoDto;
import lookIT.lookITspring.entity.Landmark;
import lookIT.lookITspring.repository.LandmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@Transactional
@ExtendWith(MockitoExtension.class)
public class LandmarkServiceTest {

    @Mock
    private LandmarkRepository landmarkRepository;

    @InjectMocks
    private LandmarkService landmarkService;

    @Test
    @DisplayName("랜드마크 리스트 조회")
    public void 랜드마크_리스트_조회() throws Exception {
        // Given
        List<Object[]> landmarkData = new ArrayList<>();
        landmarkData.add(new Object[] {1L, 18.1354, 11.2222226});
        landmarkData.add(new Object[] {2L, 19.2468, 12.333625915});
        when(landmarkRepository.findAllLandmarks()).thenReturn(landmarkData);

        // When
        List<AllLandmarkDto> allLandmarks = landmarkService.allLandmarkInfo();

        // Then
        assertEquals("Assertion failed: Number of Landmark is wrong", 2, allLandmarks.size());

        AllLandmarkDto landmark1 = allLandmarks.get(0);
        assertEquals("Assertion failed: Landmark1 Id mismatch", 1L, landmark1.getLandmarkId());
        assertEquals("Assertion failed: Landmark1 Latitude mismatch", 18.1354, landmark1.getLandLatitude());
        assertEquals("Assertion failed: Landmark1 Longitude mismatch", 11.2222226, landmark1.getLandLongitude());

        AllLandmarkDto landmark2 = allLandmarks.get(1);
        assertEquals("Assertion failed: Landmark2 Id mismatch", 2L, landmark2.getLandmarkId());
        assertEquals("Assertion failed: Landmark2 Latitude mismatch", 19.2468, landmark2.getLandLatitude());
        assertEquals("Assertion failed: Landmark2 Longitude mismatch", 12.333625915, landmark2.getLandLongitude());

    }

    @Test
    @DisplayName("랜드마크_ID로 상세정보 조회 - 존재하는 ID")
    public void 랜드마크_ID로_상세정보_조회() throws Exception {
        //Given
        Landmark landmark = Landmark.builder()
            .landmarkName("테스트용 랜드마크")
            .landLatitude(18.1354)
            .landLongitude(11.2222226)
            .landInfo("테스트를 위해 만들어진 임시 랜드마크")
            .landmarkAddress("대한민국 서울 어딘가")
            .frameUrl("https://unknown_url.com")
            .build();

        Long landmarkId = 1L;

        when(landmarkRepository.findById(landmarkId)).thenReturn(Optional.of(landmark));

        //When
        ResponseEntity<LandmarkInfoDto> actualLandmark = landmarkService.landmarkInfoWithLandmarkId(landmarkId);
        LandmarkInfoDto landmarkInfo = actualLandmark.getBody();

        //Then
        assertEquals("Assertion failed: Landmark name mismatch", "테스트용 랜드마크", landmarkInfo.getLandmarkName());
        assertEquals("Assertion failed: Landmark info mismatch", "테스트를 위해 만들어진 임시 랜드마크", landmarkInfo.getLandInfo());
        assertEquals("Assertion failed: Landmark url mismatch", "https://unknown_url.com", landmarkInfo.getFrameUrl());
        assertEquals("Assertion failed: Landmark address mismatch", "대한민국 서울 어딘가", landmarkInfo.getLandmarkAddress());
    }


    @Test
    @DisplayName("랜드마크_ID로 상세정보 조회 - 존재하지 않는 ID")
    public void testLandmarkInfoWithInvalidLandmarkId() {
        // Given
        Long landmarkId = 1L;
        when(landmarkRepository.findById(landmarkId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<LandmarkInfoDto> actualLandmark = landmarkService.landmarkInfoWithLandmarkId(landmarkId);

        // Then
        assertNull(actualLandmark.getBody(), "Assertion failed: Landmark is not empty");
    }


}
