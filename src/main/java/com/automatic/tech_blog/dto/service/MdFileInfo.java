package com.automatic.tech_blog.dto.service;

import java.util.Date;
import javax.annotation.Nullable;

public record MdFileInfo(String fileName, String id, String directory, Date createdAt, Date modifiedAt, @Nullable Date deletedAt) {}
