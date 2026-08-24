package com.docusync.dto.collaborator;

import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Add Collaborator Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorAddRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Role is required")
    private CollaboratorRole role;
}