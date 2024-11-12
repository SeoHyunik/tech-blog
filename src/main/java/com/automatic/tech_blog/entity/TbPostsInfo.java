package com.automatic.tech_blog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.Data;

@Entity
@Table(name = "tb_posts_info")
@Data
public class TbPostsInfo {
  @Id
  private int id;
  private String title;
  private String content;
  private Timestamp publishedAt;
  private String status;
}
