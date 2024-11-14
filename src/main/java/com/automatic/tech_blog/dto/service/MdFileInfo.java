package com.automatic.tech_blog.dto.service;

import com.google.api.client.util.DateTime;
import javax.annotation.Nullable;

public record MdFileInfo(String fileName, String id, String directory, DateTime createdAt, DateTime modifiedAt, @Nullable DateTime deletedAt) {}
