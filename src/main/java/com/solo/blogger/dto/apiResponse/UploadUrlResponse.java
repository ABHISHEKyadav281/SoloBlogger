package com.solo.blogger.dto.apiResponse;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadUrlResponse {
    private String uploadUrl;
    private String key;
}
