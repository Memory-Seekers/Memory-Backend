package lookIT.lookITspring.controller;

import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.config.S3FileUpload;
import lookIT.lookITspring.entity.LinePath;
import lookIT.lookITspring.service.MemorySpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/memories")
@RequiredArgsConstructor
public class MemorySpotController {
    @Autowired
    private MemorySpotService memorySpotService;

    @S3FileUpload("memoryphoto")
    @PostMapping("/upload")
    public boolean uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("spotLatitude") Double spotLatitude,
        @RequestParam("spotLongitude") Double spotLongitude,
        @RequestParam("memoryId") Long memoryId,
        HttpServletRequest request
    ) throws Exception {
        String imageUrl = (String) request.getAttribute("imageUrl");
        String s3Key = (String) request.getAttribute("s3Key");

        if (imageUrl == null || s3Key == null) {
            throw new Exception("S3 Err - imageUrl or s3Key is null");
        } else {
            return memorySpotService.createNewMemorySpot(spotLatitude, spotLongitude, memoryId, imageUrl, s3Key);
        }
    }

    @GetMapping("/photo")
    public List<Map<String, Object>> MemoryPhoto(@RequestParam("memoryId") Long memoryId)
        throws Exception {
        return memorySpotService.showAllMemorySpotPhotos(memoryId);
    }

    @GetMapping("/linePath")
    public List<LinePath> MemoryLinePath(@RequestParam("memoryId") Long memoryId) throws Exception {
        return memorySpotService.showAllLinePath(memoryId);
    }

    @DeleteMapping("/photo")
    public Boolean DeleteMemorySpotPhoto(@RequestParam("memoryPhoto") String photoUrl) {
        return memorySpotService.deletePhoto(photoUrl);
    }
}
