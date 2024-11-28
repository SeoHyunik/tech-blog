package com.automatic.tech_blog.dto.service;

import java.util.Date;
import javax.annotation.Nullable;

public record FileInfo(String fileName, String id, String directory, Date createdAt, Date modifiedAt, @Nullable Date deletedAt) {}
