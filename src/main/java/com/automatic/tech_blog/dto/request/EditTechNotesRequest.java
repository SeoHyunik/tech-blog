package com.automatic.tech_blog.dto.request;
import com.automatic.tech_blog.dto.service.FileLists;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EditTechNotesRequest(
    @NotNull(message = "GoogleAuthInfo is required") @Valid GoogleAuthInfo googleAuthInfo,
    @NotNull(message = "MdFileLists is required") @Valid FileLists fileLists
) {}
