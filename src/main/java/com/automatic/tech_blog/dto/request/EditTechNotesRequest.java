package com.automatic.tech_blog.dto.request;
import com.automatic.tech_blog.dto.service.MdFileLists;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EditTechNotesRequest(
    @NotNull(message = "GoogleAuthInfo is required") @Valid GoogleAuthInfo googleAuthInfo,
    @NotNull(message = "MdFileLists is required") @Valid MdFileLists mdFileLists
) {}
