package com.divyam.advent.service;

import com.divyam.advent.dto.PhotoCreateRequest;
import com.divyam.advent.dto.PhotoUploadSignatureResponse;
import com.divyam.advent.exception.PhotoLimitExceededException;
import com.divyam.advent.repository.PhotoRepository;
import com.divyam.advent.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceImplTest {

    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private UserRepository userRepository;

    private static final String CLOUD = "cloud";
    private static final String KEY = "key";
    private static final String SECRET = "secret";
    private static final String FOLDER = "advent-recap";
    private static final String TRANSFORM = "c_limit,w_1600,h_1600,q_auto:good";

    private PhotoServiceImpl service(int monthlyLimit) {
        return new PhotoServiceImpl(
                photoRepository, userRepository, CLOUD, KEY, SECRET, FOLDER, monthlyLimit, TRANSFORM);
    }

    @Test
    void getUploadSignature_includesTransformationAndSignsItInOrder() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);

        PhotoUploadSignatureResponse resp = service(300).getUploadSignature(1L);

        assertEquals(CLOUD, resp.getCloudName());
        assertEquals(KEY, resp.getApiKey());
        assertEquals(FOLDER, resp.getFolder());
        assertEquals(TRANSFORM, resp.getTransformation());

        // Signed params must be alphabetical: folder, timestamp, transformation.
        String payload = "folder=" + FOLDER
                + "&timestamp=" + resp.getTimestamp()
                + "&transformation=" + TRANSFORM;
        assertEquals(sha1Hex(payload + SECRET), resp.getSignature());
    }

    @Test
    void createPhoto_enforcesConfiguredMonthlyLimit() {
        PhotoServiceImpl limited = service(2);
        when(photoRepository.countByUserIdAndCreatedAtBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(2L);

        assertThrows(PhotoLimitExceededException.class,
                () -> limited.createPhoto(1L, new PhotoCreateRequest()));
    }

    @Test
    void getMonthlyLimitStatus_reflectsConfiguredCap() {
        PhotoServiceImpl limited = service(5);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(photoRepository.countByUserIdAndCreatedAtBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(2L);

        PhotoService.PhotoLimitStatusResponse status = limited.getMonthlyLimitStatus(1L);

        assertEquals(2, status.used());
        assertEquals(3, status.remaining());
        assertEquals(5, status.limit());
    }

    private static String sha1Hex(String value) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return HexFormat.of().formatHex(sha1.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
