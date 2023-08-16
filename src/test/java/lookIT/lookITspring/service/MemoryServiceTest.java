package lookIT.lookITspring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.LinePathDto;
import lookIT.lookITspring.dto.MemoryCreateRequestDto;
import lookIT.lookITspring.dto.MemoryListDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.FriendTags;
import lookIT.lookITspring.entity.InfoTags;
import lookIT.lookITspring.entity.LinePath;
import lookIT.lookITspring.entity.Memory;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.FriendTagsRepository;
import lookIT.lookITspring.repository.InfoTagsRepository;
import lookIT.lookITspring.repository.LinePathRepository;
import lookIT.lookITspring.repository.MemoryRepository;
import lookIT.lookITspring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.AssertionErrors;

@SpringBootTest
@Transactional
public class MemoryServiceTest {

	@Autowired
	private MemoryService memoryService;

	@Autowired 
	private MemoryRepository memoryRepository;

	@Autowired
    private UserService userService;

	@Autowired
    private UserRepository userRepository;

	@Autowired
    private LinePathRepository linePathRepository;

	@Autowired
    private InfoTagsRepository infoTagsRepository;

	@Autowired
	private FriendTagsRepository friendTagsRepository;

	private Long memoryId;
	private String token;

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
		token = userService.login(user1).getAccessToken();

		ArrayList<LinePathDto> pathList = new ArrayList<>();
		LinePathDto path1 = new LinePathDto(101.13, 101.2);
		LinePathDto path2 = new LinePathDto(101.13, 101.3);
		pathList.add(path1);
		pathList.add(path2);
		MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);

		memoryId = memoryService.memoryCreate(token, requestDto);
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
		memoryService.tagFriendToMemory(friendsList, memoryId);

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

	@Test
	@DisplayName("Id로 추억일지 리스트 조회 - 0개")
	public void Id로_추억일지_리스트_조회_0개() throws Exception{
		//Given
		String tagId1 ="testFriend1";
		String email1 ="testFriend1@gmail.com";
		String password1 ="memoryRecord123!";
		String nickName1 ="testFriend1";
		UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
		userService.join(userDto1);

		HashMap<String, String> user2 = new HashMap<>();
		user2.put("email", email1);
		user2.put("password", password1);
		String token1 = userService.login(user2).getAccessToken();

		//When
		Integer expected_size = memoryService.getMemoryListByToken(token1).size();

		//Then
		AssertionErrors.assertEquals("Assertion failed: Memory list not 0", 0, expected_size);
	}

	@Test
	@DisplayName("Id로 추억일지 리스트 조회 - 1개")
	public void Id로_추억일지_리스트_조회_1개() throws Exception{
		//Given
		//When
		Integer expected_size = memoryService.getMemoryListByToken(token).size();

		//Then
		AssertionErrors.assertEquals("Assertion failed: Memory list not 0", 1, expected_size);
	}

	@Test
	@DisplayName("Id로 추억일지 리스트 조회 - 여러개")
	public void Id로_추억일지_리스트_조회_여러개() throws Exception{
		//Given
		ArrayList<LinePathDto> pathList = new ArrayList<>();
		LinePathDto path1 = new LinePathDto(10.21564, 11.0216588);
		LinePathDto path2 = new LinePathDto(10.22345, 11.101);
		pathList.add(path1);
		pathList.add(path2);
		MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);
		memoryService.memoryCreate(token, requestDto);

		//When
		Integer expected_size = memoryService.getMemoryListByToken(token).size();

		//Then
		AssertionErrors.assertEquals("Assertion failed: Memory list not 0", 2, expected_size);
	}

	@Test
	@DisplayName("친구 추억일지 리스트 조회 - 0개")
	public void 친구_추억일지_리스트_조회_0개() throws Exception{
		//Given
		String tagId1 ="testFriend1";
		String email1 ="testFriend1@gmail.com";
		String password1 ="memoryRecord123!";
		String nickName1 ="testFriend1";
		UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
		userService.join(userDto1);

		//When
		Integer expected_size = memoryService.getFriendMemoryListByTagId(tagId1).size();

		//Then
		AssertionErrors.assertEquals("Assertion failed: Memory list not 0", 0, expected_size);
	}

	@Test
	@DisplayName("친구 추억일지 리스트 조회 - 1개")
	public void 친구_추억일지_리스트_조회_1개() throws Exception{
		//Given
		String tagId1 ="testFriend1";
		String email1 ="testFriend1@gmail.com";
		String password1 ="memoryRecord123!";
		String nickName1 ="testFriend1";
		UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
		userService.join(userDto1);

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email1);
		user1.put("password", password1);
		String token1 = userService.login(user1).getAccessToken();

		ArrayList<LinePathDto> pathList = new ArrayList<>();
		LinePathDto path1 = new LinePathDto(10.21564, 11.0216588);
		LinePathDto path2 = new LinePathDto(10.22345, 11.101);
		pathList.add(path1);
		pathList.add(path2);
		MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);
		memoryService.memoryCreate(token1, requestDto);

		//When
		Integer expected_size = memoryService.getFriendMemoryListByTagId(tagId1).size();

		//Then
		AssertionErrors.assertEquals("Assertion failed: Memory list not 0", 1, expected_size);
	}

	@Test
	@DisplayName("친구 추억일지 리스트 조회 - 여러개")
	public void 친구_추억일지_리스트_조회_여러개() throws Exception{
		//Given
		String tagId1 ="testFriend1";
		String email1 ="testFriend1@gmail.com";
		String password1 ="memoryRecord123!";
		String nickName1 ="testFriend1";
		UserJoinRequestDto userDto1 = new UserJoinRequestDto(tagId1, email1, password1, nickName1);
		userService.join(userDto1);

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email1);
		user1.put("password", password1);
		String token1 = userService.login(user1).getAccessToken();

		ArrayList<LinePathDto> pathList = new ArrayList<>();
		LinePathDto path1 = new LinePathDto(10.21564, 11.0216588);
		LinePathDto path2 = new LinePathDto(10.22345, 11.101);
		pathList.add(path1);
		pathList.add(path2);
		MemoryCreateRequestDto requestDto = new MemoryCreateRequestDto(pathList);
		memoryService.memoryCreate(token1, requestDto);

		ArrayList<LinePathDto> pathList2 = new ArrayList<>();
		LinePathDto path3 = new LinePathDto(11.54654, 68.16549);
		LinePathDto path4 = new LinePathDto(11.56989, 68.36549);
		pathList2.add(path3);
		pathList2.add(path4);
		MemoryCreateRequestDto requestDto2 = new MemoryCreateRequestDto(pathList2);
		memoryService.memoryCreate(token1, requestDto2);

		//When
		Integer expected_size = memoryService.getFriendMemoryListByTagId(tagId1).size();

		//Then
		AssertionErrors.assertEquals("Assertion failed: Memory list not 0", 2, expected_size);
	}

    @Test
    @DisplayName("정보태그 검색")
	public void 정보태그_검색() throws Exception{
		//Given
		HashMap<String, String> map1 = new HashMap<>();
		map1.put("info", "xxyyzz");
		HashMap<String, String> map2 = new HashMap<>();
		map2.put("info", "for_searching_info_tag");
		List<Map<String, String>> request = new ArrayList<>();

		request.add(map1);
		request.add(map2);

		memoryService.createInfoTags(memoryId, request);

		//When
		MemoryListDto memory = memoryService.searchMemoryByInfoTags(token, "for_searching_info_tag").get(0);

		//Then
		assert memory.getInfo().contains("for_searching_info_tag");
		assert memory.getInfo().contains("xxyyzz");
		AssertionErrors.assertEquals("Assertion failed: Memory info tag size is not 2", 2, memory.getInfo().size());
	}

	@Test
	@DisplayName("친구태그 생성")
	public void 친구태그_생성() throws Exception{
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

		//When
		memoryService.tagFriendToMemory(friendsList, memoryId);

		//Then
		Memory taggedMemory = memoryRepository.findById(memoryId).get();
		List<FriendTags> friendTags = friendTagsRepository.findByFriendTagsId_Memory(taggedMemory);

		assertNotNull(friendTags);
		AssertionErrors.assertEquals("Assertion failed: Number of tagged friends is not 2", 2, friendTags.size());
		assert(friendTags.contains("testFriend1"));
		assert(friendTags.contains("testFriend2"));
	}

}
