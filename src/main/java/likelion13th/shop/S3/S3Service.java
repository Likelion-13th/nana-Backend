package likelion13th.shop.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service  // Spring 서비스 컴포넌트
@RequiredArgsConstructor  // final 필드 자동 주입
public class S3Service {

    private final AmazonS3 amazonS3;  // S3Config에서 만든 Bean이 주입됨
    private final S3Properties s3Properties;

    // S3에 파일 업로드 후 URL 반환
    public String uploadFile(MultipartFile file) {
        String bucketName = s3Properties.getBucket();
        // 파일명 충돌 방지를 위해 UUID 사용
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // 파일 메타데이터 설정 (파일 타입, 크기)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

            // 업로드된 파일의 S3 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }
}