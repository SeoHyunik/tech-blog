package com.automatic.tech_blog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;

@Entity
@Table(name = "tb_posts_info")
@Data
public class TbPostsInfo {
  @Id private int id;
  private String title;
  private int contentId;
  private String fileId;
  private Date publishedAt;
  private String status;
}
