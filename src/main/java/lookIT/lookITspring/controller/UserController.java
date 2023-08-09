package lookIT.lookITspring.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public boolean join(@RequestBody UserJoinRequestDto request) {
        return userService.join(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> member) {
        return userService.login(member);
    }

    @GetMapping("/join/exists")
    public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam("tagId") String tagId) {
        return ResponseEntity.ok(userService.checkIdDuplicate(tagId));
    }

    @DeleteMapping("/logout")
    public boolean logout(@RequestBody Map<String, String> request) {
        return userService.logout(request.get("token"));
    }

    @PostMapping("/emailConfirm")
    public String emailConfirm(@RequestParam String email) throws Exception {
        return userService.emailConfirm(email);
    }

    @PostMapping("/emailConfirmJoin")
    public String emailConfirmJoin(@RequestParam String email) throws Exception {
        return userService.emailConfirmJoin(email);
    }

    @PostMapping("/findPassword")
    public boolean regeneratePassword(@RequestBody Map<String, String> request) {
        return userService.regeneratePassword(request);
    }
}