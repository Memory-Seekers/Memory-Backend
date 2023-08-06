package lookIT.lookITspring.service;

import java.util.HashMap;
import javax.transaction.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.entity.*;
import lookIT.lookITspring.repository.CollectionsRepository;
import lookIT.lookITspring.repository.LandmarkRepository;
import lookIT.lookITspring.repository.PhotoTagsRepository;
import lookIT.lookITspring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
public class Photo4CutService {

    private final LandmarkRepository landmarkRepository;
    private final CollectionsRepository collectionsRepository;
    private final UserRepository userRepository;
    private final PhotoTagsRepository photoTagsRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    private AmazonS3 s3Client;

    public String getPhotoFrame(long landmarkId) throws Exception {
        String frameUrl = landmarkRepository.findById(landmarkId)
                .orElseThrow(() -> new Exception("No landmark found for the given landmarkId."))
                .getFrameUrl();

        if (frameUrl == null) {
            throw new Exception("No landmarkFrame for the given landmark.");
        }

        return frameUrl;
    }

    public Long savePhoto4Cut(Long landmarkId, Long userId, String imageUrl, String key) {
        Landmark landmark = landmarkRepository.findById(landmarkId)
            .orElseThrow(() -> new IllegalArgumentException("landmark not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Collections collection = Collections.builder()
            .createAt(LocalDateTime.now())
            .user(user)
            .landmark(landmark)
            .photo4Cut(imageUrl)
            .photo4CutKey(key)
            .build();

        collectionsRepository.save(collection);
        return collection.getPhoto4CutId();
    }

    public List<Collections> getCollectionsByUserId(Long userId) {
        List<Collections> collections = collectionsRepository.findAllByUserIdOrderByCreateAtDesc(
            userId);

        return collections;
    }


    public List<Collections> getCollectionsByTagId(String tagId) {

        List<PhotoTags> photoTags = photoTagsRepository.findByTagId(tagId);
        List<Long> photo4CutIds = photoTags.stream()
            .map(PhotoTags::getCollections)
            .map(Collections::getPhoto4CutId)
            .collect(Collectors.toList());
        return collectionsRepository.findByPhoto4CutIdInOrderByCreateAtDesc(photo4CutIds);
    }

    public String collectionFriendTag(String[] friendsList, Long photo4CutId) {
        Collections collection = collectionsRepository.findById(photo4CutId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid photo4CutId"));

        if (friendsList.length != 0) {
            for (String friend : friendsList) {
                PhotoTags photoTags = PhotoTags.builder()
                    .tagId(friend)
                    .collections(collection)
                    .build();
                photoTagsRepository.save(photoTags);
            }
            return "Friends tagged successfully to photo4Cut";
        } else {
            return "No friends to tag to photo4Cut";
        }
    }

    public void collectionFriendTagDelete(Long photo4CutId) {
        List<PhotoTags> photoTags = photoTagsRepository.findByCollectionsPhoto4CutId(photo4CutId);
        photoTagsRepository.deleteAll(photoTags);
    }

    public List<Map<String, String>> getTaggedFriendListByPhoto4CutIdId(Long photo4CutId) {
        Collections collections = collectionsRepository.findById(photo4CutId).get();
        List<PhotoTags> photoTags = photoTagsRepository.findByCollectionsPhoto4CutId(photo4CutId);
        List<Map<String, String>> friendList = new ArrayList<>();

        for (PhotoTags friend : photoTags) {
            String tagId = friend.getTagId();
            User user = userRepository.findByTagId(tagId).get();
            Map<String, String> friendMap = new HashMap<>();
            friendMap.put("nickName", user.getNickName());
            friendMap.put("tagId", user.getTagId());
            friendList.add(friendMap);
        }
        return friendList;
    }

    private void deletePhotoFromS3(String key) throws Exception {
        try {
            boolean isS3Object = s3Client.doesObjectExist(bucket, key);
            if (isS3Object) {
                s3Client.deleteObject(bucket, key);
            } /*else {
                throw new Exception("S3 object does not exist for the given key.");
            }*/
        } catch (Exception e) {
            throw new Exception("Failed - Delete S3 file", e);
        }
    }

    public boolean deletePhoto4Cut(Long photo4CutId) throws Exception{

            Optional<Collections> collectionOptional = collectionsRepository.findById(photo4CutId);
            Collections collection = collectionOptional.orElseThrow(
                () -> new Exception("No collection found for the given photo4CutId."));

            collectionFriendTagDelete(photo4CutId); //추억네컷 친구 태그 삭제

            String photo4CutKey = collection.getPhoto4CutKey();
            deletePhotoFromS3(photo4CutKey);
            collectionsRepository.delete(collection);

        return true;
    }
}

