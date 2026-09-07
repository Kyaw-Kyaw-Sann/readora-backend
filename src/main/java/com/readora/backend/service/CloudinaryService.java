package com.readora.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.readora.backend.dto.response.MediaUploadResponse;
import com.readora.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final String COVER_FOLDER = "readora/covers";
    private static final String PDF_FOLDER = "readora/pdfs";
    private static final String AUDIO_FOLDER = "readora/audio";

    @Value("${cloudinary.max-cover-size:10485760}")
    private long maxCoverSize;

    @Value("${cloudinary.max-pdf-size:104857600}")
    private long maxPdfSize;

    @Value("${cloudinary.max-audio-size:209715200}")
    private long maxAudioSize;

    private final Cloudinary cloudinary;

    public MediaUploadResponse uploadCover(MultipartFile file) {
        validateFile(file);
        validateCover(file);

        return upload(file, COVER_FOLDER, "image");
    }

    public MediaUploadResponse uploadPdf(MultipartFile file) {
        validateFile(file);
        validatePdf(file);

        return upload(file, PDF_FOLDER, "raw");
    }

    public MediaUploadResponse uploadAudio(MultipartFile file) {
        validateFile(file);
        validateAudio(file);

        return upload(file, AUDIO_FOLDER, "video");
    }

    public MediaUploadResponse replaceCover(MultipartFile file, String oldPublicId) {

        MediaUploadResponse newUpload = uploadCover(file);

        if (hasPublicId(oldPublicId)) {
            deleteCover(oldPublicId);
        }

        return newUpload;
    }

    public MediaUploadResponse replacePdf(MultipartFile file, String oldPublicId) {

        MediaUploadResponse newUpload = uploadPdf(file);

        if (hasPublicId(oldPublicId)) {
            deletePdf(oldPublicId);
        }

        return newUpload;
    }

    public MediaUploadResponse replaceAudio(MultipartFile file, String oldPublicId) {

        MediaUploadResponse newUpload = uploadAudio(file);

        if (hasPublicId(oldPublicId)) {
            deleteAudio(oldPublicId);
        }

        return newUpload;
    }

    public void deleteCover(String publicId) {
        delete(publicId, "image");
    }

    public void deletePdf(String publicId) {
        delete(publicId, "raw");
    }

    public void deleteAudio(String publicId) {
        delete(publicId, "video");
    }

    private MediaUploadResponse upload(MultipartFile file, String folder, String resourceType) {

        // file.getInputStream() အစား file.getBytes() ကို
        // ပြောင်းလဲအသုံးပြုထားပါသည်
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", resourceType));

            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            if (secureUrl == null || publicId == null) {
                throw new BadRequestException("Cloudinary did not return valid upload information");
            }

            return new MediaUploadResponse(secureUrl, publicId);

        } catch (BadRequestException ex) {
            throw ex;

        } catch (Exception ex) {
            ex.printStackTrace();

            throw new BadRequestException("Failed to upload file: " + getErrorMessage(ex));
        }
    }

    private void delete(String publicId, String resourceType) {

        if (!hasPublicId(publicId)) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType, "invalidate", true));

        } catch (Exception ex) {
            ex.printStackTrace();

            throw new BadRequestException("Failed to delete file: " + getErrorMessage(ex));
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
    }

    private void validateCover(MultipartFile file) {

        validateFileSize(file, maxCoverSize, "Cover image");

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {

            throw new BadRequestException("Cover file must be an image");
        }
    }

    private void validatePdf(MultipartFile file) {

        validateFileSize(file, maxPdfSize, "PDF file");

        String contentType = file.getContentType();

        if (!"application/pdf".equalsIgnoreCase(contentType)) {

            throw new BadRequestException("File must be a PDF");
        }
    }

    private void validateAudio(MultipartFile file) {

        validateFileSize(file, maxAudioSize, "Audio file");

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("audio/")) {

            throw new BadRequestException("File must be an audio file");
        }
    }

    private void validateFileSize(MultipartFile file, long maxSize, String fileType) {

        if (file.getSize() > maxSize) {
            throw new BadRequestException(fileType + " must not exceed " + bytesToMegabytes(maxSize) + "MB");
        }
    }

    private long bytesToMegabytes(long bytes) {
        return bytes / (1024 * 1024);
    }

    private boolean hasPublicId(String publicId) {
        return publicId != null && !publicId.isBlank();
    }

    private String getErrorMessage(Exception ex) {

        if (ex.getMessage() == null || ex.getMessage().isBlank()) {

            return "Unknown Cloudinary error";
        }

        return ex.getMessage();
    }
}