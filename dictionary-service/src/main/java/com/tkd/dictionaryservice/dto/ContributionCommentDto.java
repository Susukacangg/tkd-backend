package com.tkd.dictionaryservice.dto;

import com.tkd.dictionaryservice.entity.ContributionCommentEntity;
import lombok.Data;

@Data
public class ContributionCommentDto {
    private Long commentId;
    private Long wordId;
    private String username;
    private String comment;
    private String commentDateTime;
    private String editedDateTime;
    private Boolean isEdited;
    private Boolean isDeleted;

    public ContributionCommentDto(ContributionCommentEntity entity, String username) {
        this.commentId = entity.getCommentId();
        this.wordId = entity.getWordId();
        this.username = entity.getIsDeleted() ? null : username;
        this.comment = entity.getIsDeleted() ? null : entity.getComment();
        this.commentDateTime = entity.getCommentDateTime().toString();
        this.editedDateTime = entity.getIsDeleted() ? null : (entity.getEditedDateTime() != null ? entity.getEditedDateTime().toString() : null);
        this.isEdited = !entity.getIsDeleted() && entity.getIsEdited();
        this.isDeleted = entity.getIsDeleted();
    }
}
