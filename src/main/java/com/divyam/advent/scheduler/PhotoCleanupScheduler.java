package com.divyam.advent.scheduler;

import com.divyam.advent.model.Photo;
import com.divyam.advent.repository.PhotoRepository;
import com.divyam.advent.service.CloudinaryCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PhotoCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PhotoCleanupScheduler.class);

    private final PhotoRepository photoRepository;
    private final CloudinaryCleanupService cloudinaryCleanupService;

    public PhotoCleanupScheduler(
            PhotoRepository photoRepository,
            CloudinaryCleanupService cloudinaryCleanupService
    ) {
        this.photoRepository = photoRepository;
        this.cloudinaryCleanupService = cloudinaryCleanupService;
    }

    @Scheduled(cron = "0 0 2 1 * *")
    public void purgeLastMonthPhotos() {
        LocalDate lastMonthAnchor = LocalDate.now().minusMonths(1);
        LocalDateTime start = lastMonthAnchor.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = lastMonthAnchor.withDayOfMonth(1).plusMonths(1).atStartOfDay().minusNanos(1);

        List<Photo> photos = photoRepository.findByCreatedAtBetweenAndPublicIdIsNotNull(start, end);
        int purged = 0;
        int failed = 0;

        for (Photo photo : photos) {
            String publicId = photo.getPublicId();
            boolean destroyed = cloudinaryCleanupService.destroyOnCloudinary(publicId);

            if (destroyed) {
                photo.setPublicId(null);
                photo.setSecureUrl(null);
                photoRepository.save(photo);
                purged++;
            } else {
                logger.warn(
                        "Cloudinary cleanup failed for photo id {} with publicId {}",
                        photo.getId(),
                        publicId
                );
                failed++;
            }
        }

        logger.info("Cleanup complete: {} purged, {} failed out of {} total.", purged, failed, photos.size());
    }
}
