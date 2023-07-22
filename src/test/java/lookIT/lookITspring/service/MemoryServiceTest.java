package lookIT.lookITspring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.InfoTags;
import lookIT.lookITspring.entity.LinePath;
import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.InfoTagsRepository;
import lookIT.lookITspring.repository.LinePathRepository;
import lookIT.lookITspring.repository.MemoryRepository;
import lookIT.lookITspring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class MemoryServiceTest {

	@Autowired MemoryService memoryService;
	@Autowired MemoryRepository memoryRepository;
	@Autowired UserService userService;
	@Autowired UserRepository userRepository;
	@Autowired LinePathRepository linePathRepository;
	@Autowired InfoTagsRepository infoTagsRepository;

	Long memoryId;

	@BeforeEach
	void setUp() throws Exception{
		String tagId ="test";
		String email ="test@gmail.com";
		String password ="memoryRecord123!";
		String nickName ="test";
		UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId, email, password, nickName);
		userService.join(userDto1);

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);
		String token1 = userService.login(user1);

		ArrayList<LinePathDto> pathList = new ArrayList<>();
		LinePathDto path1 = new LinePathDto(101.13, 101.2);
		LinePathDto path2 = new LinePathDto(101.13, 101.3);
		pathList.add(path1);
		pathList.add(path2);
		MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);

		memoryId = memoryService.memoryCreate(token1, requestDto);
	}

	@Test
	@DisplayName("추억일지 생성_성공")
	public void memoryCreateSuccess() throws Exception {
		//Then
		Memory memory = memoryRepository.findById(memoryId).get();
		User user = userRepository.findByEmail("test@gmail.com").get();
		List<LinePath> findPath = linePathRepository.findByMemory(memory);

		assertEquals(user.getTagId(), memory.getUser().getTagId());
		assertEquals(101.13, findPath.get(0).getLatitude());
		assertEquals(101.2, findPath.get(0).getLongitude());
		assertEquals(101.13, findPath.get(1).getLatitude());
		assertEquals(101.3, findPath.get(1).getLongitude());
	}

	@Test
	@DisplayName("정보 태그 생성_성공")
	public void createInfoTagsSuccess() throws Exception {
		//Given
		HashMap<String, String> map1 = new HashMap<>();
		map1.put("info", "sea");
		HashMap<String, String> map2 = new HashMap<>();
		map2.put("info", "surfing");
		List<Map<String, String>> request = new ArrayList<>();
		request.add(map1);
		request.add(map2);

		//When
		memoryService.createInfoTags(memoryId, request);

		//Then
		Memory memory = memoryRepository.findById(memoryId).get();
		List<InfoTags> infoTagList = infoTagsRepository.findByInfoTagsIdMemory(memory);
		assertEquals("sea", infoTagList.get(0).getInfoTagsId().getInfo());
		assertEquals("surfing", infoTagList.get(1).getInfoTagsId().getInfo());
	}

	@Test
	@DisplayName("추억일지 태그된 친구 리스트 조회_성공")
	public void getTaggedFriendListByMemoryIdSuccess() throws Exception {
		//Given
		String tagId1 ="testFriend1";
		String email1 ="testFriend1@gmail.com";
		String password1 ="memoryRecord123!";
		String nickName1 ="testFriend1";
		UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
		userService.join(userDto1);

		String tagId2 ="testFriend2";
		String email2 ="testFriend2@gmail.com";
		String password2 ="memoryRecord123!";
		String nickName2 ="testFriend2";
		UserJoinRequestDto userDto2 = new UserJoinRequestDto(tagId2, email2, password2, nickName2);
		userService.join(userDto2);

		String[] friendsList= {"testFriend1", "testFriend2"};
		memoryService.memoryFriendTag(friendsList, memoryId);

		//When
		List<Map<String,String>> findFriendList = memoryService.getTaggedFriendListByMemoryId(memoryId);

		//Then
		assertEquals("testFriend1", findFriendList.get(0).get("tagId"));
		assertEquals("testFriend2", findFriendList.get(1).get("tagId"));
	}

	@Test
	@DisplayName("정보 태그 삭제_성공")
	public void deleteInfoTagSuccess() throws Exception{
		//Given
		HashMap<String, String> map1 = new HashMap<>();
		map1.put("info", "sea");
		List<Map<String, String>> request = new ArrayList<>();
		request.add(map1);
		memoryService.createInfoTags(memoryId, request);

		Map<String, String> infoId = new HashMap<>();
		infoId.put("memoryId", Long.toString(memoryId));
		infoId.put("info", "sea");

		//When
		memoryService.deleteInfoTag(infoId);

		//Then
		assertEquals(0, infoTagsRepository.findByInfoTagsIdMemory(memoryRepository.findById(memoryId).get()).size());
	}
}
