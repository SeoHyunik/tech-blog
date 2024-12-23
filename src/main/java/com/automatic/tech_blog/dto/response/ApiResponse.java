package com.automatic.tech_blog.dto.response;

import java.util.Date;

public record ApiResponse(int status, Object data, Date timestamp, boolean isSuccess) {}