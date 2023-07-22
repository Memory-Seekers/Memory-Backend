package lookIT.lookITspring.service;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.FriendListDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.Friends;
import lookIT.lookITspring.repository.FriendsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class FriendServiceTest {

    @Autowired
    FriendsRepository friendsRepository;

    @Autowired
    FriendService friendService;

    @Autowired
    UserService userService;

    private String token;
    private String token2;
    private String token3;

    @BeforeEach
    void beforeEach() throws Exception {

        String tagId = "userTagId";
        String email = "user1@gmail.com";
        String password = "memoryRecord123!";
        String nickName = "userTest";
        UserJoinRequestDto user = new UserJoinRequestDto(tagId, email, password, nickName);

        String friend1TagId = "friend1x1xy2yz3z";
        String friend1Email = "friend1@example.com";
        String friend1Password = "1password123!";
        String friend1NickName = "friend1Test";
        UserJoinRequestDto friend1 = new UserJoinRequestDto(friend1TagId, friend1Email, friend1Password, friend1NickName);

        String friend2TagId = "friend2TagId";
        String friend2Email = "friend2@example.com";
        String friend2Password = "2password123!";
        String friend2NickName = "friend2Test";
        UserJoinRequestDto friend2 = new UserJoinRequestDto(friend2TagId, friend2Email, friend2Password, friend2NickName);

        userService.join(user);
        userService.join(friend1);
        userService.join(friend2);

        HashMap<String, String> userToken = new HashMap<>();
        userToken.put("email", email);
        userToken.put("password", password);

        token = userService.login(userToken);

        HashMap<String, String> friend1Token = new HashMap<>();
        friend1Token.put("email", friend1Email);
        friend1Token.put("password", friend1Password);

        token2 = userService.login(friend1Token);

        HashMap<String, String> friend2Token = new HashMap<>();
        friend2Token.put("email", friend2Email);
        friend2Token.put("password", friend2Password);

        token3 = userService.login(friend2Token);
    }

    @Test
    @DisplayName("특정 태그 Id를 포함하는 친구목록 조회 - 0명")
    public void 특정_태그_Id를_포함하는_친구목록_조회_0명() {
        //Given
        Integer size = -1;

        //When
        List<FriendListDto> foundFriends = friendService.friendInfoIncludingTagId("x1xy2yz3zk4k", token);

        //Then
        Integer numOfFriends = foundFriends.size();
        if (numOfFriends.equals(0)) {
            size = null;
        } else {
            size = numOfFriends;
        }
        assertNull(size, "Assertion failed: Number of friends including given Id is not zero");
    }

    @Test
    @DisplayName("특정 태그 Id를 포함하는 친구목록 조회 - 1명")
    public void 특정_태그_Id를_포함하는_친구목록_조회_1명() {
        //Given
        Integer size = -1;

        //When
        List<FriendListDto> foundFriends = friendService.friendInfoIncludingTagId("x1xy2yz3z", token);

        //Then
        Integer numOfFriends = foundFriends.size();
        if (numOfFriends.equals(0)) {
            size = null;
        } else {
            size = numOfFriends;
        }
        assertEquals("Assertion failed: Number of friends including given Id is not one", 1, size);
    }

    @Test
    @DisplayName("특정 태그 Id를 포함하는 친구목록 조회 - 여러명(2명 이상)")
    public void 특정_태그_Id를_포함하는_친구목록_조회_여러명() {
        //Given
        Integer size = -1;

        //When
        List<FriendListDto> foundFriends = friendService.friendInfoIncludingTagId("friend", token);

        //Then
        Integer numOfFriends = foundFriends.size();
        if (numOfFriends.equals(0)) {
            size = null;
        } else {
            size = numOfFriends;
        }
        assert size > 1 : "Assertion failed: Number of friends including given Id is under two";
    }

    @Test
    @DisplayName("내 정보 조회")
    public void 내_정보_조회() {
        //Given
        //When
        FriendListDto myInfo = friendService.myInfo(token);

        //Then
        assertEquals("Assertion failed: Landmark2 Id mismatch", "userTagId", myInfo.getTagId());
        assertEquals("Assertion failed: Landmark2 Latitude mismatch", "userTest", myInfo.getNickName());
    }

    @Test
    @DisplayName("내가 친구요청 보내기")
    public void 내가_친구요청_보내기() {
        //Given
        friendsRepository.deleteAll();

        //When
        boolean request = friendService.friendRequest("friend1x1xy2yz3z" ,token);
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(friends -> "R".equals(friends.getStatus()))
            .filter(friends -> "userTagId".equals(friends.getFriendsId().getUser().getTagId()))
            .collect(Collectors.toList());
        Friends savedRequest = friendsList.get(0);

        //Then
        assertTrue(request, "Friend request was successful");
        assertEquals("Number of friend requests in the database is not one", 1, friendsList.size());

        assertEquals("Unexpected status for the friend request", "R", savedRequest.getStatus());
        assertEquals("Unexpected tagId for the user", "friend1x1xy2yz3z", savedRequest.getFriendsId().getFriend().getTagId());
    }

    @Test
    @DisplayName("내가 보낸 친구요청 취소")
    public void 내가_보낸_친구요청_취소() {
        //Given
        friendsRepository.deleteAll();
        friendService.friendRequest("friend1x1xy2yz3z" ,token);
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(friends -> "R".equals(friends.getStatus()))
            .filter(friends -> "userTagId".equals(friends.getFriendsId().getUser().getTagId()))
            .collect(Collectors.toList());

        //When
        friendService.myRequestCancel("friend1x1xy2yz3z", token);
        List<Friends> friendsList2 = friendsRepository.findAll().stream()
            .filter(friends -> "R".equals(friends.getStatus()))
            .filter(friends -> "userTagId".equals(friends.getFriendsId().getUser().getTagId()))
            .collect(Collectors.toList());

        //Then
        assertEquals("Number of friend requests in the database is not one", 1, friendsList.size());
        assertEquals("Number of friend requests in the database is not empty", 0, friendsList2.size());
    }

    @Test
    @DisplayName("내가 받은 친구요청 리스트 - 0명")
    public void 내가_받은_친구요청_리스트_0명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;

        //When
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(friends -> "R".equals(friends.getStatus()))
            .filter(friends -> "userTagId".equals(friends.getFriendsId().getFriend().getTagId()))
            .collect(Collectors.toList());

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertNull(size, "Assertion failed: Number of friends including given Id is not zero");
    }

    @Test
    @DisplayName("내가 받은 친구요청 리스트 - 1명")
    public void 내가_받은_친구요청_리스트_1명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("userTagId" ,token2);

        //When
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(friends -> "R".equals(friends.getStatus()))
            .filter(friends -> "userTagId".equals(friends.getFriendsId().getFriend().getTagId()))
            .collect(Collectors.toList());

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertEquals("Assertion failed: Number of friend request is not one", 1, size);
    }

    @Test
    @DisplayName("내가 받은 친구요청 리스트 - 여러명")
    public void 내가_받은_친구요청_리스트_여러명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("userTagId" ,token2);
        friendService.friendRequest("userTagId",token3);

        //When
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(friends -> "R".equals(friends.getStatus()))
            .filter(friends -> "userTagId".equals(friends.getFriendsId().getFriend().getTagId()))
            .collect(Collectors.toList());

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertEquals("Assertion failed: Number of friend request is not two", 2, size);
    }

    @Test
    @DisplayName("받은 친구요청 수락")
    public void 받은_친구요청_수락() {
        //Given
        friendsRepository.deleteAll();

        //When
        friendService.friendRequest("userTagId" ,token2);
        friendService.friendAccept("friend1x1xy2yz3z" ,token);
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(friends -> "A".equals(friends.getStatus()))
            .filter(user -> "userTagId".equals(user.getFriendsId().getFriend().getTagId()))
            .collect(Collectors.toList());

        Friends savedRequest = friendsList.get(0);

        //Then
        assertEquals("Number of friend request accepted is not one", 1, friendsList.size());
        assertEquals("Unexpected friend's TagId for the friend request", "friend1x1xy2yz3z", savedRequest.getFriendsId().getUser().getTagId());
    }

    @Test
    @DisplayName("받은 친구요청 거절")
    public void 받은_친구요청_거절() {
        //Given
        friendsRepository.deleteAll();

        //When
        friendService.friendRequest("friend1x1xy2yz3z" ,token);
        friendService.friendReject("userTagId" ,token2);
        List<Friends> friendsList = friendsRepository.findAll().stream()
            .filter(user -> "userTagId".equals(user.getFriendsId().getUser().getTagId()))
            .collect(Collectors.toList());

        //Then
        assertEquals("Number of friend request accepted is not one", 0, friendsList.size());
    }

    @Test
    @DisplayName("내가 보낸 친구요청 리스트 - 0명")
    public void 내가_보낸_친구요청_리스트_0명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;

        //When
        List<FriendListDto> friendsList = friendService.myRequestList(token);

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertNull(size, "Assertion failed: Number of friend request I've sent is not zero");
    }

    @Test
    @DisplayName("내가 보낸 친구요청 리스트 - 1명")
    public void 내가_보낸_친구요청_리스트_1명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("friend1x1xy2yz3z" ,token);

        //When
        List<FriendListDto> friendsList = friendService.myRequestList(token);

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertEquals("Assertion failed: Number of friend request is not one", 1, size);
    }

    @Test
    @DisplayName("내가 보낸 친구요청 리스트 - 여러명")
    public void 내가_보낸_친구요청_리스트_여러명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("friend1x1xy2yz3z" ,token);
        friendService.friendRequest("friend2TagId",token);

        //When
        List<FriendListDto> friendsList = friendService.myRequestList(token);

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertEquals("Assertion failed: Number of friend request is not two", 2, size);
    }

    @Test
    @DisplayName("내 친구 리스트 - 0명")
    public void 내_친구_리스트_0명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("friend1x1xy2yz3z" ,token);
        friendService.friendRequest("friend2TagId",token);

        //When
        List<FriendListDto> myFriendList = friendService.getMyfriendList(token);
        Integer numOfFriendRequest = myFriendList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertNull(size, "Assertion failed: Number of friends is not zero");
    }

    @Test
    @DisplayName("내 친구 리스트 - 1명")
    public void 내_친구_리스트_1명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("friend1x1xy2yz3z" ,token);
        friendService.friendRequest("friend2TagId",token);

        //When
        friendService.friendAccept("userTagId" ,token2);
        List<FriendListDto> friendsList = friendService.getMyfriendList(token);

        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertEquals("Assertion failed: Number of friends is not one", 1, size);
    }

    @Test
    @DisplayName("내 친구 리스트 - 여러명")
    public void 내_친구_리스트_여러명() {
        //Given
        friendsRepository.deleteAll();
        Integer size = -1;
        friendService.friendRequest("friend1x1xy2yz3z" ,token);
        friendService.friendRequest("friend2TagId",token);

        //When
        friendService.friendAccept("userTagId" ,token2);
        friendService.friendAccept("userTagId",token3);

        List<FriendListDto> friendsList = friendService.getMyfriendList(token);
        Integer numOfFriendRequest = friendsList.size();
        if (numOfFriendRequest.equals(0)) {
            size = null;
        } else {
            size = numOfFriendRequest;
        }

        //Then
        assertEquals("Assertion failed: Number of friends is not two", 2, size);
    }

}
