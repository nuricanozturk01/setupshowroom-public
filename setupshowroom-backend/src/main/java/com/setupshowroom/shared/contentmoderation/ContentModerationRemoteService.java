package com.setupshowroom.shared.contentmoderation;

import com.setupshowroom.shared.contentmoderation.dto.SightEngineVideoResponse;
import com.setupshowroom.shared.contentmoderation.dto.SightengineImageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
    name = "sightengineClient",
    url = "https://api.sightengine.com/1.0",
    configuration = FeignSupportConfig.class)
public interface ContentModerationRemoteService {
  @PostMapping(value = "/check.json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  SightengineImageResponse analyzeImage(
      @RequestPart("media") MultipartFile media,
      @RequestPart("models") String models,
      @RequestPart("api_user") String apiUser,
      @RequestPart("api_secret") String apiSecret);

  @PostMapping(value = "/video/check-sync.json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  SightEngineVideoResponse analyzeVideo(
      @RequestPart("media") MultipartFile media,
      @RequestPart("models") String models,
      @RequestPart("api_user") String apiUser,
      @RequestPart("api_secret") String apiSecret);
}
