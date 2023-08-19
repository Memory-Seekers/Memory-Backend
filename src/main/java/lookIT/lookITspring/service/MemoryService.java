package lookIT.lookITspring.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.transaction.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.FriendTagsDto;
import lookIT.lookITspring.dto.InfoTagsDto;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.MemoryListDto;
import lookIT.lookITspring.entity.FriendTags;
import lookIT.lookITspring.entity.FriendTagsId;
import lookIT.lookITspring.entity.InfoTags;
import lookIT.lookITspring.entity.InfoTagsId;
import lookIT.lookITspring.entity.LinePath;
import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.MemoryPhoto;
import lookIT.lookITspring.entity.MemorySpot;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.FriendTagsRepository;
import lookIT.lookITspring.repository.InfoTagsRepository;
import lookIT.lookITspring.repository.LinePathRepository;
import lookIT.lookITspring.repository.MemoryPhotoRepository;
import lookIT.lookITspring.repository.MemoryRepository;
import lookIT.lookITspring.repository.MemorySpotRepository;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
@Transactional
public class MemoryService {

    private final UserRepository userRepository;
    private final MemoryRepository memoryRepository;
    private final LinePathRepository linePathRepository;
    private final FriendTagsRepository friendTagsRepository;
    private final InfoTagsRepository infoTagsRepository;
    private final MemorySpotRepository memorySpotRepository;
    private final MemoryPhotoRepository memoryPhotoRepository;
    private final JwtProvider jwtProvider;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private AmazonS3 s3Client;

    public Long createMemory(String token, MemoryCreateRequestDto requestDto) {
            Long userId = jwtProvider.getUserId(token);
            User user = userRepository.findById(userId).get();
            Memory memory = Memory.builder()
                .user(user)
                .build();
            Memory savedMemory = memoryRepository.save(memory);

            for (LinePathDto lp : requestDto.getPath()) {
                LinePath linePath = LinePath.builder()
                    .latitude(lp.getLatitude())
                    .longitude(lp.getLongitude())
                    .memory(savedMemory)
                    .build();
                linePathRepository.save(linePath);
            }
            return savedMemory.getMemoryId();
    }

    public List<MemoryListDto> getMemoryListByToken(String token) {
        Long userId = jwtProvider.getUserId(token);
        List<Memory> memories = memoryRepository.findByUser_UserId(userId);
        List<MemoryListDto> myMemoryList = new ArrayList<>();
        for (Memory memory : memories) {
            MemoryListDto memoryListDto = createMemoryListDto(memory);
            myMemoryList.add(memoryListDto);
        }
        //시간순 정렬
        Collections.reverse(myMemoryList);
        return myMemoryList;
    }

    public List<MemoryListDto> getFriendMemoryListByTagId(String tagId) {
        List<Memory> memories = memoryRepository.findByUser_tagId(tagId);
        List<MemoryListDto> friendMemoryList = new ArrayList<>();
        for (Memory memory : memories) {
            MemoryListDto memoryListDto = createMemoryListDto(memory);
            friendMemoryList.add(memoryListDto);
        }
        //시간순 정렬
        Collections.reverse(friendMemoryList);
        return friendMemoryList;
    }

    public List<MemoryListDto> searchMemoryByInfoTags(String token, String info) {
        Long userId = jwtProvider.getUserId(token);
        List<InfoTags> infoTagsList = infoTagsRepository.findByInfoTagsIdInfo(info);
        List<MemoryListDto> memoryIncludingInfoTags = new ArrayList<>();
        for (InfoTags infoTags : infoTagsList) {
            Memory memory = infoTags.getInfoTagsId().getMemory();
            if (memory.getUser().getUserId().equals(userId)) {
                MemoryListDto memoryListDto = createMemoryListDto(memory);
                memoryIncludingInfoTags.add(memoryListDto);
            }
        }
        Collections.reverse(memoryIncludingInfoTags);
        return memoryIncludingInfoTags;
    }

    private MemoryListDto createMemoryListDto(Memory memory) {
        Long memoryId = memory.getMemoryId();
        List<MemorySpot> memorySpots = memorySpotRepository.findAllByMemory(memory);
        String memoryPhoto = "";

        if (!memorySpots.isEmpty()) {
            MemorySpot memorySpot = memorySpots.get(0);
            List<MemoryPhoto> memoryPhotos = memoryPhotoRepository.findAllByMemorySpot(memorySpot);
            if (!memoryPhotos.isEmpty()) {
                MemoryPhoto memoryPhotoEntity = memoryPhotos.get(0);
                memoryPhoto = memoryPhotoEntity.getMemoryPhoto();
            }
        }

        LocalDateTime createAt = memory.getCreateAt();
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime zonedDateTime = createAt.atZone(zoneId);
        ZonedDateTime zonedDateTimeWithOffset = zonedDateTime.plusHours(13);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd E", Locale.KOREA);
        String createAtFormatted = zonedDateTimeWithOffset.format(formatter);
        List<InfoTagsDto> info = getInfoTagList(memoryId);
        List<FriendTagsDto> friends = getFriendTagList(memoryId);

        return new MemoryListDto(memoryId, memoryPhoto, createAtFormatted, info, friends);
    }

    private List<FriendTagsDto> getFriendTagList(Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId).get();
        List<FriendTags> friendTagsList = friendTagsRepository.findByFriendTagsId_Memory(memory);
        List<FriendTagsDto> friendTagList = new ArrayList<>();
        for (FriendTags friendTags : friendTagsList) {
            User user = friendTags.getFriendTagsId().getUser();
            String tagId = user.getTagId();
            friendTagList.add(new FriendTagsDto(tagId));
        }
        return friendTagList;
    }

    private List<InfoTagsDto> getInfoTagList(Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId).get();
        List<InfoTags> infoTagsList = infoTagsRepository.findByInfoTagsIdMemory(memory);
        List<InfoTagsDto> infoTagList = new ArrayList<>();
        for (InfoTags infoTag : infoTagsList) {
            String info = infoTag.getInfoTagsId().getInfo();
            infoTagList.add(new InfoTagsDto(info));
        }
        return infoTagList;
    }

    public String tagFriendToMemory(String[] friendsList, Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId).get();

        if (friendsList.length != 0) {
            for (String friend : friendsList) {
                User friendToTag = userRepository.findByTagId(friend).orElseThrow(() -> new IllegalArgumentException("Invalid tagId"));
                FriendTagsId friendTagsId = new FriendTagsId(memory, friendToTag);
                FriendTags friendTags = new FriendTags(friendTagsId);
                friendTagsRepository.save(friendTags);
            }
            return "Tagged successfully";
        } else {
            return "No friend to tag";
        }
    }

    public boolean createInfoTags(Long memoryId, List<Map<String, String>> request) {
        Memory memory = memoryRepository.findById(memoryId).get();
        for (Map<String, String> tag : request) {
            InfoTagsId infoTagsId = InfoTagsId.builder()
                .memory(memory)
                .info(tag.get("info"))
                .build();
            InfoTags infoTags = InfoTags.builder().infoTagsId(infoTagsId).build();
            infoTagsRepository.save(infoTags);
        }
        return true;
    }

    public List<Map<String, String>> getFriendTagListByMemoryId(Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId).get();
        List<FriendTags> friendTags = friendTagsRepository.findByFriendTagsId_Memory(memory);
        List<Map<String, String>> friendTagList = new ArrayList<>();

        for (FriendTags friend : friendTags) {
            Long userId = friend.getFriendTagsId().getUser().getUserId();
            User user = userRepository.findById(userId).get();
            Map<String, String> friendMap = new HashMap<>();
            friendMap.put("nickName", user.getNickName());
            friendMap.put("tagId", user.getTagId());
            friendTagList.add(friendMap);
        }
        return friendTagList;
    }

    public boolean deleteInfoTag(Map<String, String> infoId) {
        Long memoryId = Long.parseLong(infoId.get("memoryId"));
        Memory memory = memoryRepository.findById(memoryId).get();
        InfoTagsId infoTagsId = InfoTagsId.builder()
            .memory(memory)
            .info(infoId.get("info"))
            .build();
        infoTagsRepository.deleteById(infoTagsId);
        return true;
    }

    public void deleteLinePath(Memory memory) {
        linePathRepository.deleteAllByMemory(memory);
    }

    public void deleteFriendTag(Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId).get();
        List<FriendTags> friendTags = friendTagsRepository.findByFriendTagsId_Memory(memory);
        for (FriendTags friend : friendTags) {
            Long userId = friend.getFriendTagsId().getUser().getUserId();
            User user = userRepository.findById(userId).get();
            FriendTagsId friendTagsId = new FriendTagsId(memory, user);
            friendTagsRepository.deleteByFriendTagsId(friendTagsId);
        }
    }

    private void deletePhotoFromS3(String key) {
        try {
            boolean isS3Object = s3Client.doesObjectExist(bucket, key);
            if (isS3Object) {
                s3Client.deleteObject(bucket, key);
            } else {
                throw new Exception("S3 object does not exist for the given key.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed - Delete S3 file", e);
        }
    }

    private void deleteSpotPhoto(String photoUrl) {
        MemoryPhoto memoryPhoto = memoryPhotoRepository.findByMemoryPhoto(photoUrl);

        if (memoryPhoto != null) {
            deletePhotoFromS3(memoryPhoto.getMemoryPhotoKey());
            memoryPhotoRepository.delete(memoryPhoto);
            MemorySpot memorySpot = memoryPhoto.getMemorySpot();
            memorySpotRepository.delete(memorySpot);
        } else {
            throw new IllegalArgumentException("Memory photo not found.");
        }
    }

    private void deleteMemorySpot(Long memoryId) {
        List<MemorySpot> memorySpots = memorySpotRepository.findByMemoryMemoryId(memoryId);
        if (memorySpots.isEmpty()) {
            System.out.println("No MemorySpot found for the given memoryId.");
            return;
        }

        for (MemorySpot memorySpot : memorySpots) {
            MemoryPhoto memoryPhoto = memoryPhotoRepository.findByMemorySpotSpotId(
                memorySpot.getSpotId());
            deleteSpotPhoto(memoryPhoto.getMemoryPhoto());
        }

    }

    public boolean deleteMemory(String token, Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId).get();
        deleteLinePath(memory);
        deleteFriendTag(memoryId);
        infoTagsRepository.deleteAllByInfoTagsIdMemory(memory);
        deleteMemorySpot(memoryId);
        memoryRepository.delete(memory);
        return true;
    }
}
