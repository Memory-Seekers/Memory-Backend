package lookIT.lookITspring.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.MemoryListDto;
import lookIT.lookITspring.service.MemoryService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/memories")
@RestController
public class MemoryController {

    private final MemoryService memoryService;

    @PostMapping("/create")
    public Long createMemory(@RequestHeader String token, @RequestBody MemoryCreateRequestDto request) {
        return memoryService.createMemory(token, request);
    }

    @PostMapping("/info")
    public boolean createInfoTags(@RequestParam("memoryId") Long memoryId,
        @RequestBody List<Map<String, String>> request) {
        return memoryService.createInfoTags(memoryId, request);
    }

    @GetMapping("/info")
    public List<MemoryListDto> getMemoryByInfoTags(@RequestHeader("token") String token,
        @RequestParam String info) {
        return memoryService.searchMemoryByInfoTags(token, info);
    }

    @PostMapping("/info/delete")
    public boolean deleteInfoTag(@RequestBody Map<String, String> infoId) {
        return memoryService.deleteInfoTag(infoId);
    }

    @GetMapping("/list")
    @ResponseBody
    public List<MemoryListDto> getMemoryList(@RequestHeader("token") String token) {
        return memoryService.getMemoryListByToken(token);
    }

    @GetMapping("/friendList")
    @ResponseBody
    public List<MemoryListDto> getFriendMemoryList(@RequestParam String tagId) {
        return memoryService.getFriendMemoryListByTagId(tagId);
    }

    @PostMapping("/friendTag")
    public String postMemoryFriendTag(@RequestBody String[] friendsList,
        @RequestParam Long memoryId) {
        return memoryService.tagFriendToMemory(friendsList, memoryId);
    }

    @GetMapping("/taggedFriendList")
    @ResponseBody
    public List<Map<String, String>> getFriendTagList(@RequestParam Long memoryId) {
        return memoryService.getFriendTagListByMemoryId(memoryId);
    }

    @DeleteMapping("")
    public boolean deleteMemory(@RequestHeader String token, @RequestParam Long memoryId) {
        return memoryService.deleteMemory(token, memoryId);
    }

}
