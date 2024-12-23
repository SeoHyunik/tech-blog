package com.automatic.tech_blog.dto.response;


import java.util.Date;

public record ErrorResponse(int status, String errMsg, Date timestamp, boolean isSuccess) {}