package lookIT.lookITspring.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

public class AwsS3Interceptor extends HandlerInterceptorAdapter {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private AmazonS3 s3Client;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            if (method.isAnnotationPresent(S3FileUpload.class)) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                MultipartFile file = multipartRequest.getFile("file");

                if (file != null) {
                    try{
                        String folderName = method.getAnnotation(S3FileUpload.class).value();
                        System.out.println(folderName);
                        String imageUrl = uploadFileAndGetImageUrl(file, folderName);
                        String key = folderName + "/" + file.getOriginalFilename();

                        request.setAttribute("imageUrl", imageUrl);
                        request.setAttribute("s3Key", key);

                        System.out.println("imageUrl: " + imageUrl);
                        System.out.println("s3Key: " + key);

                        return true;
                    }catch(NullPointerException e){
                        throw new NullPointerException("S3 client is null");
                    }
                }
            }
        }
        return false;
    }

    public String uploadFileAndGetImageUrl(MultipartFile file, String folderName) throws Exception {
        try {
            String fileName = file.getOriginalFilename();
            LocalDateTime now = LocalDateTime.now();
            String nowTime = now.toString();
            String key = folderName + "/" + fileName + nowTime;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(bucket, key, file.getInputStream(), metadata);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            s3Client.putObject(request);

            String imageUrl = s3Client.getUrl(bucket, key).toString();

            return imageUrl;
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }
}
