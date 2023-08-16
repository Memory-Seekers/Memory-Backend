package lookIT.lookITspring.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Configuration
@Profile("test")
public class MockAwsS3Config extends S3config{
    @Bean
    @Override
    public AmazonS3 amazonS3Client(){
        AmazonS3 s3ClientMock = Mockito.mock(AmazonS3.class);
        S3Object mockS3Object = Mockito.mock(S3Object.class);
        S3ObjectInputStream mockObjectInputStream = new S3ObjectInputStream(new ByteArrayInputStream(new byte[0]), null);
        ObjectMetadata mockObjectMetadata = new ObjectMetadata();
        mockObjectMetadata.setContentType("image/jpeg");

        Mockito.when(s3ClientMock.getUrl(Mockito.anyString(), Mockito.anyString())).thenAnswer((Answer<URL>) invocation -> {
            String bucketName = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
            String encodedPhotoUrl = "http://example.com/" + bucketName + "/" + encodedKey;
            System.out.println("Encoded URL : " + encodedPhotoUrl);
            return new URL(encodedPhotoUrl);
        });

        Mockito.when(s3ClientMock.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(mockS3Object);
        Mockito.when(mockS3Object.getObjectContent()).thenReturn(mockObjectInputStream);
        Mockito.when(mockS3Object.getObjectMetadata()).thenReturn(mockObjectMetadata);

        return s3ClientMock;
    }

}
