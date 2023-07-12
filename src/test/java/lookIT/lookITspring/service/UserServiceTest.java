package lookIT.lookITspring.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.transaction.Transactional;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class UserServiceTest {

	@Autowired UserService userService;
	@Autowired UserRepository userRepository;

	@Test
	@DisplayName("회원가입_정상")
	public void joinSuccess() throws Exception {
		//Given
		String tagId ="lookIt-test1";
		String email ="thelookit06@gmail.com";
		String password ="memoryRecord123!";
		String nickName ="lookIt-test";

		UserJoinRequestDto user1 = new UserJoinRequestDto(tagId, email, password, nickName);

		//When
		boolean b = userService.join(user1);

		//Then
		User findUser = userRepository.findByEmail(email).get();
		assertEquals(tagId, findUser.getTagId());
	}

	@Test
	@DisplayName("회원가입_중복회원예외")
	public void joinFail() throws Exception {
		//Given
		String tagId ="lookIt-test1";
		String email ="thelookit06@gmail.com";
		String password ="memoryRecord123!";
		String nickName ="lookIt-test";

		UserJoinRequestDto user1 = new UserJoinRequestDto(tagId, email, password, nickName);
		UserJoinRequestDto user2 =  new UserJoinRequestDto(tagId, email, password, nickName);

		//When
		userService.join(user1);
		IllegalStateException e = assertThrows(IllegalStateException.class,
			() -> userService.join(user2));
		assertThat(e.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
	}
}
