package lookIT.lookITspring.config;

import java.util.Properties;
import lookIT.lookITspring.repository.CollectionsRepository;
import lookIT.lookITspring.repository.FriendTagsRepository;
import lookIT.lookITspring.repository.FriendsRepository;
import lookIT.lookITspring.repository.InfoTagsRepository;
import lookIT.lookITspring.repository.LandmarkRepository;
import lookIT.lookITspring.repository.LinePathRepository;
import lookIT.lookITspring.repository.MemoryPhotoRepository;
import lookIT.lookITspring.repository.MemoryRepository;
import lookIT.lookITspring.repository.PhotoTagsRepository;
import lookIT.lookITspring.repository.RefreshTokenRepository;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.repository.MemorySpotRepository;
import lookIT.lookITspring.security.CustomUserDetailsService;
import lookIT.lookITspring.security.JwtProvider;
import lookIT.lookITspring.service.EmailService;
import lookIT.lookITspring.service.FriendService;
import lookIT.lookITspring.service.LandmarkService;
import lookIT.lookITspring.service.MemoryService;
import lookIT.lookITspring.service.Photo4CutService;
import lookIT.lookITspring.service.RefreshTokenService;
import lookIT.lookITspring.service.UserService;
import lookIT.lookITspring.service.MemorySpotService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableRedisRepositories
public class SpringConfig implements WebMvcConfigurer {

    @Value("${mail.smtp.port}")
    private int port;
    @Value("${mail.smtp.socketFactory.port}")
    private int socketPort;
    @Value("${mail.smtp.auth}")
    private boolean auth;
    @Value("${mail.smtp.starttls.enable}")
    private boolean starttls;
    @Value("${mail.smtp.starttls.required}")
    private boolean startlls_required;
    @Value("${mail.smtp.socketFactory.fallback}")
    private boolean fallback;
    @Value("${AdminMail.id}")
    private String id;
    @Value("${AdminMail.password}")
    private String password;

    private final UserRepository userRepository;
    private final MemorySpotRepository memorySpotRepository;
    private final LandmarkRepository landmarkRepository;
    private final MemoryRepository memoryRepository;
    private final LinePathRepository linePathRepository;
    private final FriendTagsRepository friendTagsRepository;
    private final InfoTagsRepository infoTagsRepository;
    private final FriendsRepository friendsRepository;
    private final MemoryPhotoRepository memoryPhotoRepository;
    private final PhotoTagsRepository photoTagsRepository;
    private final CollectionsRepository collectionsRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public SpringConfig(UserRepository userRepository,
        MemorySpotRepository memorySpotRepository, LandmarkRepository landmarkRepository,
        MemoryRepository memoryRepository, LinePathRepository linePathRepository,
        FriendTagsRepository friendTagsRepository, InfoTagsRepository infoTagsRepository,
        FriendsRepository friendsRepository, MemoryPhotoRepository memoryPhotoRepository,
        PhotoTagsRepository photoTagsRepository, CollectionsRepository collectionsRepository, RefreshTokenRepository refreshTokenRepository) {

        this.userRepository = userRepository;
        this.memorySpotRepository = memorySpotRepository;
        this.landmarkRepository = landmarkRepository;
        this.memoryRepository = memoryRepository;
        this.linePathRepository = linePathRepository;
        this.friendTagsRepository = friendTagsRepository;
        this.infoTagsRepository = infoTagsRepository;
        this.friendsRepository = friendsRepository;
        this.memoryPhotoRepository = memoryPhotoRepository;
        this.photoTagsRepository = photoTagsRepository;
        this.collectionsRepository = collectionsRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtProvider jwtProvider() {
        return new JwtProvider(customUserDetailsService(), userRepository);
    }

    @Bean
    public JavaMailSender javaMailService() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setUsername(id);
        javaMailSender.setPassword(password);
        javaMailSender.setPort(port);
        javaMailSender.setJavaMailProperties(getMailProperties());
        javaMailSender.setDefaultEncoding("UTF-8");
        return javaMailSender;
    }

    private Properties getMailProperties() {
        Properties pt = new Properties();
        pt.put("mail.smtp.socketFactory.port", socketPort);
        pt.put("mail.smtp.auth", auth);
        pt.put("mail.smtp.starttls.enable", starttls);
        pt.put("mail.smtp.starttls.required", startlls_required);
        pt.put("mail.smtp.socketFactory.fallback", fallback);
        pt.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return pt;
    }

    @Bean
    public EmailService emailService() {
        return new EmailService(javaMailService());
    }

    @Bean
    public UserService userService() {
        return new UserService(userRepository, passwordEncoder(), jwtProvider(), emailService(), refreshTokenService(), refreshTokenRepository);
    }

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public MemorySpotService memorySpotService(MemorySpotRepository memorySpotRepository,
        MemoryRepository memoryRepository, MemoryPhotoRepository memoryPhotoRepository,
        LinePathRepository linePathRepository) {
        return new MemorySpotService(memorySpotRepository, memoryRepository, memoryPhotoRepository,
            linePathRepository);
    }

    @Bean
    public MemoryService memoryService() {
        return new MemoryService(userRepository, memoryRepository, linePathRepository,
            friendTagsRepository, infoTagsRepository, memorySpotRepository, memoryPhotoRepository,
            jwtProvider());
    }

    @Bean
    public LandmarkService landmarkService() {
        return new LandmarkService(landmarkRepository);
    }

    @Bean
    public FriendService friendService() {
        return new FriendService(userRepository, friendsRepository, jwtProvider());
    }

    @Bean
    public Photo4CutService photo4CutService() {
        return new Photo4CutService(landmarkRepository, collectionsRepository, userRepository,
            photoTagsRepository);
    }

    @Bean
    public RefreshTokenService refreshTokenService() {
        return new RefreshTokenService(refreshTokenRepository, userRepository, jwtProvider());
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(awsS3Interceptor()).addPathPatterns("/memories/upload");
        registry.addInterceptor(awsS3Interceptor()).addPathPatterns("/collections/4cutphoto");
    }

    @Bean
    public AwsS3Interceptor awsS3Interceptor() {
        return new AwsS3Interceptor();
    }

}
