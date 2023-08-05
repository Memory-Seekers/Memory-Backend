package lookIT.lookITspring.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.FriendListDto;
import lookIT.lookITspring.entity.Friends;
import lookIT.lookITspring.entity.FriendsId;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.FriendsRepository;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;

@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final UserRepository userRepository;
    private final FriendsRepository friendsRepository;
    private final JwtProvider jwtProvider;

    public List<FriendListDto> friendInfoIncludingTagId(String tagId, String token) {
        Long userId = jwtProvider.getUserId(token);
        User user = userRepository.findById(userId).get();
        List<FriendListDto> myFriendList = getMyfriendList(token);
        List<FriendListDto> myRequestList = myRequestList(token);

        List<User> friends = userRepository.findAll();
        List<FriendListDto> friendIncludingTagId = new ArrayList<>();
        for (User friend : friends) {
            if (friend.getTagId().contains(tagId)) {
                FriendListDto friendListDto = new FriendListDto(
                    friend.getTagId(),
                    friend.getNickName());
                friendIncludingTagId.add(friendListDto);
            }
        }

        List<FriendListDto> filterFriendIncludingTagId = friendIncludingTagId.stream()
            .filter(friend -> !friend.getTagId().equals(user.getTagId()))
            .filter(friend -> myFriendList.stream().noneMatch(myFriend -> myFriend.getTagId().equals(friend.getTagId())))
            .filter(friend -> myRequestList.stream().noneMatch(myFriend -> myFriend.getTagId().equals(friend.getTagId())))
            .map(friend -> new FriendListDto(friend.getTagId(), friend.getNickName()))
            .collect(Collectors.toList());

        return filterFriendIncludingTagId;
    }

    public FriendListDto myInfo(String token) {
        Long userId = jwtProvider.getUserId(token);
        User user = userRepository.findById(userId).get();
        FriendListDto myInformation = new FriendListDto(
            user.getTagId(),
            user.getNickName()
        );
        return myInformation;
    }

    public boolean friendRequest(String tagId, String token) {
        Long userId = jwtProvider.getUserId(token);
        User friend = userRepository.findByTagId(tagId).get();
        User user = userRepository.findById(userId).get();
        Friends friends = new Friends(friend, user, "R");
        friendsRepository.save(friends);
        return true;
    }

    public boolean myRequestCancel(String tagId, String token) {
        Long userId = jwtProvider.getUserId(token);
        User friend = userRepository.findByTagId(tagId).get();
        User user = userRepository.findById(userId).get();

        FriendsId friendsId = new FriendsId(friend, user);
        Friends checkRequest = friendsRepository.findById(friendsId).orElse(null);
        if (checkRequest != null && checkRequest.getStatus().equals("R")) {
            friendsRepository.delete(checkRequest);
            return true;
        } else {
            return false;
        }
    }

    public List<FriendListDto> recievedRequestList(String token) {
        Long userId = jwtProvider.getUserId(token);
        List<Friends> myFriends = friendsRepository.findByFriendsId_Friend_UserIdAndStatus(userId, "R");
        List<FriendListDto> recievedRequestList = new ArrayList<>();
        for (Friends myFriend : myFriends) {
            FriendListDto friendListDto = new FriendListDto(
                myFriend.getFriendsId().getFriend().getTagId(),
                myFriend.getFriendsId().getFriend().getNickName());
            recievedRequestList.add(friendListDto);
        }
        return recievedRequestList;
    }

    public boolean friendAccept(String tagId, String token) {
        Long userId = jwtProvider.getUserId(token);
        User user = userRepository.findByTagId(tagId).get();
        User friend = userRepository.findById(userId).get();

        FriendsId friendsId = new FriendsId(friend, user);
        Friends checkRequest = friendsRepository.findById(friendsId).orElse(null);
        if (checkRequest != null) {
            checkRequest.setStatus("A");
            friendsRepository.save(checkRequest);
            return true;
        } else {
            System.out.println("친구 요청이 되지 않은 상태입니다.");
            return false;
        }
    }

    public boolean friendReject(String tagId, String token) {
        Long userId = jwtProvider.getUserId(token);
        User user = userRepository.findByTagId(tagId).get();
        User friend = userRepository.findById(userId).get();

        FriendsId friendsId = new FriendsId(friend, user);
        Friends checkRequest = friendsRepository.findById(friendsId).orElse(null);
        if (checkRequest != null) {
            friendsRepository.delete(checkRequest);
            return true;
        } else {
            System.out.println("친구 요청이 되지 않은 상태입니다.");
            return false;
        }
    }

    private List<FriendListDto> getFriendListByStatus(String token, String status) {
        Long userId = jwtProvider.getUserId(token);
        List<Friends> myFriends = friendsRepository.findByFriendsId_User_UserIdAndStatus(userId, status);
        List<FriendListDto> friendList = new ArrayList<>();

        for (Friends myFriend : myFriends) {
            FriendListDto friendListDto = new FriendListDto(
                myFriend.getFriendsId().getFriend().getTagId(),
                myFriend.getFriendsId().getFriend().getNickName());
            friendList.add(friendListDto);
        }
        return friendList;
    }

    public List<FriendListDto> myRequestList(String token) {
        return getFriendListByStatus(token, "R");
    }

    public List<FriendListDto> getMyfriendList(String token) {
        return getFriendListByStatus(token, "A");
    }

}
