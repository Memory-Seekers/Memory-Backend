package lookIT.lookITspring.controller;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.config.S3FileUpload;
import lookIT.lookITspring.entity.Collections;
import lookIT.lookITspring.security.JwtProvider;
import lookIT.lookITspring.service.Photo4CutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class Photo4cutController {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private AmazonS3 s3Client;

    private final JwtProvider jwtProvider;
    private final Photo4CutService photo4CutService;

    @GetMapping("/4cutphoto")
    public String PhotoFrame(@RequestParam("landmarkId") Long landmarkId) throws Exception {
        return photo4CutService.getPhotoFrame(landmarkId);
    }

    @S3FileUpload("photo4cut/photo")
    @PostMapping("/4cutphoto")
    public Long uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("landmarkId") Long landmarkId,
        @RequestHeader("token") String token,
        HttpServletRequest request
    ) throws Exception {
        Long userId = jwtProvider.getUserId(token);
        String imageUrl = (String) request.getAttribute("imageUrl");
        String s3Key = (String) request.getAttribute("s3Key");

        if (imageUrl == null || s3Key == null) {
            throw new Exception("S3 Err - imageUrl or s3Key is null");
        } else {
            return photo4CutService.savePhoto4Cut(landmarkId, userId, imageUrl, s3Key);
        }
    }

    @GetMapping("")
    public List<Collections> getMyMemory4Cut(@RequestHeader("token") String token) throws Exception {
        Long userId = jwtProvider.getUserId(token);
        return photo4CutService.getCollectionsByUserId(userId);
    }

    @GetMapping("/{tagId}")
    public List<Collections> FriendMemory4Cut(@PathVariable("tagId") String tagId)
        throws Exception {
        return photo4CutService.getCollectionsByTagId(tagId);
    }

    @PostMapping("/tag")
    public String TagPhoto4Cut(@RequestBody String[] friendsList, @RequestParam Long photo4CutId) {
        return photo4CutService.collectionFriendTag(friendsList, photo4CutId);
    }

    @GetMapping("/taggedFriendList")
    @ResponseBody
    public List<Map<String, String>> getTaggedFriendListByPhoto4CutIdId(
        @RequestParam Long photo4CutId) {
        return photo4CutService.getTaggedFriendListByPhoto4CutIdId(photo4CutId);
    }

    @DeleteMapping("/4CutPhotoDelete")
    public boolean delete4CutPhoto(@RequestParam Long photo4CutId) throws Exception {
        return photo4CutService.deletePhoto4Cut(photo4CutId);
    }
}

