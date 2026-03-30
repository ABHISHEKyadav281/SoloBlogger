package com.solo.blogger.dto.apiRequest;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadUrlRequest {
    private String fileName;
    private String contentType;
}
