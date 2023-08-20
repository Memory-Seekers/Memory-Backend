package lookIT.lookITspring.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.FriendListDto;
import lookIT.lookITspring.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public List<FriendListDto> getUserByTagId(@RequestParam String tagId,
        @RequestHeader(value="X-AUTH-TOKEN") String token) {
        return friendService.getFriendInfoIncludingTagId(tagId, token);
    }

    @GetMapping("/my")
    public FriendListDto getMyInfo(@RequestHeader(value="X-AUTH-TOKEN") String token) {
        return friendService.getMyInfo(token);
    }

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.OK)
    public boolean sendFriendRequest(@RequestParam String tagId, @RequestHeader(value="X-AUTH-TOKEN") String token) throws Exception {
        return friendService.sendFriendRequest(tagId, token);
    }

    @PostMapping("/accept")
    @ResponseStatus(HttpStatus.OK)
    public boolean acceptFriendRequest(@RequestParam String tagId, @RequestHeader(value="X-AUTH-TOKEN") String token)
        throws Exception {
        return friendService.acceptFriendRequest(tagId, token);
    }

    @DeleteMapping("/reject")
    public boolean rejectFriendRequest(@RequestParam String tagId, @RequestHeader(value="X-AUTH-TOKEN") String token)
        throws Exception {
        return friendService.rejectFriendRequest(tagId, token);
    }

    @GetMapping("/request")
    public List<FriendListDto> getMyRequest(@RequestHeader(value="X-AUTH-TOKEN") String token) {
        return friendService.getMyRequestList(token);
    }

    @DeleteMapping("/request")
    public boolean cancelFriendRequest(@RequestParam String tagId,
        @RequestHeader(value="X-AUTH-TOKEN") String token) {
        return friendService.cancelMyRequest(tagId, token);
    }

    @GetMapping("/accept")
    public List<FriendListDto> getFriendRequest(@RequestHeader(value="X-AUTH-TOKEN") String token) {
        return friendService.recievedRequestList(token);
    }

    @GetMapping("/list")
    public List<FriendListDto> getMyFriendList(@RequestHeader(value="X-AUTH-TOKEN") String token) {
        return friendService.getMyfriendList(token);
    }

}
