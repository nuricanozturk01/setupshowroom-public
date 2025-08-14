package com.setupshowroom.comment.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CommentForm {
  @Size(max = 300)
  private String content;
}
